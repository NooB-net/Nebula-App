package com.nebula.app.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.Html;

import com.google.gson.Gson;
import com.nebula.app.R;
import com.nebula.app.data.model.SpacePhoto;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import retrofit2.Response;

/**
 * Gives the app a rotating set of real space photographs (with descriptions) from NASA's
 * Image Library. The "photo of the day" is deterministic per calendar day, so the Home card and the
 * daily notification always show the same one. All methods here BLOCK: call them off the main thread.
 */
public final class SpacePhotoRepository {
    private SpacePhotoRepository() {}

    /** One topic per day, cycling. Edit freely. */
    private static final String[] TOPICS = {
            "Orion Nebula", "Andromeda Galaxy", "Pillars of Creation", "Saturn rings",
            "Jupiter Great Red Spot", "Mars surface rover", "Crab Nebula", "Webb deep field",
            "Carina Nebula", "Whirlpool Galaxy", "Milky Way", "total solar eclipse",
            "aurora from space station", "Hubble spiral galaxy", "supernova remnant", "comet",
            "Earthrise Moon", "Pluto New Horizons", "Sombrero Galaxy", "Horsehead Nebula",
            "Eagle Nebula", "Apollo lunar surface", "Neptune Voyager", "Europa Jupiter moon",
            "Cat's Eye Nebula", "Tarantula Nebula", "Saturn moon Titan", "Sun solar flare"
    };

    private static final String PREFS = "nebula_space_photo";

    /** Blocking. Today's photo; cached for the day, falls back to a bundled photo if offline. */
    public static SpacePhoto today(Context ctx) {
        SharedPreferences sp = ctx.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String key = dayKey();
        Gson gson = new Gson();
        if (key.equals(sp.getString("day", null))) {
            try {
                SpacePhoto cached = gson.fromJson(sp.getString("photo", null), SpacePhoto.class);
                if (cached != null && cached.getTitle() != null) return cached;
            } catch (Exception ignored) { }
        }
        try {
            List<SpacePhoto> list = fetchTopic(TOPICS[dayNumber() % TOPICS.length]);
            if (!list.isEmpty()) {
                SpacePhoto pick = list.get((dayNumber() * 7 + 3) % list.size());
                sp.edit().putString("day", key).putString("photo", gson.toJson(pick)).apply();
                return pick;
            }
        } catch (Exception ignored) { }
        return fallback(dayNumber());
    }

    /** Blocking. A random photo from a random topic (used by the Shuffle button). Null if offline. */
    public static SpacePhoto random() {
        try {
            Random rnd = new Random();
            List<SpacePhoto> list = fetchTopic(TOPICS[rnd.nextInt(TOPICS.length)]);
            if (!list.isEmpty()) return list.get(rnd.nextInt(list.size()));
        } catch (Exception ignored) { }
        return null;
    }

    // ---- Home screen auto-rotation -------------------------------------------------------------
    private static final Object ROTATION_LOCK = new Object();
    private static List<SpacePhoto> rotationCache;
    private static String rotationCacheDay;

    /**
     * Blocking. The playlist the Home card cycles through: today's photo first, then a few photos
     * from several different topics. Cached for the day once it was fetched online; offline it is
     * today's photo plus the bundled ones.
     */
    public static List<SpacePhoto> rotation(Context ctx) {
        synchronized (ROTATION_LOCK) {
            if (rotationCache != null && dayKey().equals(rotationCacheDay)) return new ArrayList<>(rotationCache);
        }
        SpacePhoto today = today(ctx);
        List<SpacePhoto> out = new ArrayList<>();
        out.add(today);

        Random rnd = new Random();
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < TOPICS.length; i++) order.add(i);
        Collections.shuffle(order, rnd);
        int topicsFetched = 0;
        for (int k = 0; k < order.size() && topicsFetched < 4; k++) {
            try {
                List<SpacePhoto> list = fetchTopic(TOPICS[order.get(k)]);
                if (list.isEmpty()) continue;
                Collections.shuffle(list, rnd);
                for (int j = 0; j < Math.min(3, list.size()); j++) {
                    if (!sameAs(today, list.get(j))) out.add(list.get(j));
                }
                topicsFetched++;
            } catch (Exception e) {
                break;       // offline: stop trying
            }
        }
        if (topicsFetched == 0) {
            for (int i = 0; i < 4; i++) {
                SpacePhoto f = fallback(i);
                if (!sameAs(today, f)) out.add(f);
            }
        } else {
            synchronized (ROTATION_LOCK) {
                rotationCache = new ArrayList<>(out);
                rotationCacheDay = dayKey();
            }
        }
        return out;
    }

    /**
     * Blocking. The space "headline" for the current hour: a different topic every hour, so the
     * hourly notification never repeats the same picture twice in a row.
     */
    public static SpacePhoto forHour() {
        long hour = System.currentTimeMillis() / 3_600_000L;
        try {
            List<SpacePhoto> list = fetchTopic(TOPICS[(int) ((hour * 5) % TOPICS.length)]);
            if (!list.isEmpty()) return list.get((int) ((hour * 7 + 3) % list.size()));
        } catch (Exception ignored) { }
        return fallback((int) (hour % 4));
    }

    private static boolean sameAs(SpacePhoto a, SpacePhoto b) {
        if (a.getNasaId() != null && b.getNasaId() != null) return a.getNasaId().equals(b.getNasaId());
        return a.getTitle() != null && a.getTitle().equals(b.getTitle());
    }

    /** Blocking. Usable photos (with a picture and a real description) for one topic. */
    static List<SpacePhoto> fetchTopic(String topic) throws IOException {
        Response<NasaImagesResponse> resp = ApiClient.getNasaImagesApi().search(topic, "image", 40).execute();
        List<SpacePhoto> out = new ArrayList<>();
        NasaImagesResponse body = resp.body();
        if (!resp.isSuccessful() || body == null || body.collection == null || body.collection.items == null) return out;

        for (NasaImagesResponse.Item item : body.collection.items) {
            if (item.data == null || item.data.isEmpty() || item.links == null || item.links.isEmpty()) continue;
            NasaImagesResponse.Data d = item.data.get(0);
            String thumb = null;
            for (NasaImagesResponse.Link l : item.links) {
                if (l.href != null && (l.rel == null || "preview".equals(l.rel))) { thumb = l.href; break; }
            }
            String desc = clean(d.description);
            if (thumb == null || d.title == null || desc.length() < 60) continue;
            thumb = thumb.replace("http://", "https://");

            SpacePhoto p = new SpacePhoto();
            p.setNasaId(d.nasaId);
            p.setTitle(clean(d.title));
            p.setDescription(desc);
            p.setThumbUrl(thumb);
            p.setImageUrl(thumb.replace("~thumb.", "~medium."));
            p.setDate(prettyDate(d.dateCreated));
            p.setCredit(d.photographer != null ? d.photographer
                    : d.secondaryCreator != null ? d.secondaryCreator
                    : d.center != null ? "NASA/" + d.center : "NASA");
            out.add(p);
        }
        return out;
    }

    // ---- bundled offline fallbacks, each with a call-to-action hook for the notification ----
    private static SpacePhoto fallback(int day) {
        String[][] f = {
                {"Orion Nebula", "Watch the Orion Nebula tonight",
                        "The Orion Nebula (M42) is a glowing stellar nursery about 1,300 light-years away. Look just below the three belt stars of Orion: the fuzzy middle \"star\" of his sword is the nebula, visible to the naked eye and stunning in binoculars."},
                {"The Full Moon", "Find the Moon tonight",
                        "The Moon is the only other world humans have walked on. Even binoculars reveal craters, mountains and the dark lava plains we call seas. The terminator, the line between light and shadow, shows the most detail."},
                {"A meteor streak", "Catch a shooting star",
                        "Most meteors are grains of dust no bigger than a pebble, burning up 100 km above you at tens of kilometres per second. Give your eyes 20 minutes to adapt to the dark and look away from the Moon."},
                {"Earth at night", "See our world from above",
                        "From orbit, cities glow like constellations. Astronauts on the ISS see this every 90 minutes as they circle the planet at 28,000 km/h."}
        };
        int[] res = {R.drawable.img_orion_nebula, R.drawable.img_event_fullmoon,
                R.drawable.img_event_meteor, R.drawable.earth_lights};
        int i = day % f.length;
        SpacePhoto p = new SpacePhoto();
        p.setTitle(f[i][0]);
        p.setHook(f[i][1]);
        p.setDescription(f[i][2]);
        p.setDate("Offline pick");
        p.setCredit("NEBULA");
        p.setLocalRes(res[i]);
        return p;
    }

    private static String clean(String s) {
        if (s == null) return "";
        return Html.fromHtml(s, Html.FROM_HTML_MODE_COMPACT).toString().replace('\n', ' ').replaceAll("\\s+", " ").trim();
    }

    private static String prettyDate(String iso) {
        if (iso == null || iso.length() < 10) return "";
        try {
            SimpleDateFormat in = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            in.setTimeZone(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat out = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
            out.setTimeZone(TimeZone.getTimeZone("UTC"));
            return out.format(in.parse(iso.substring(0, 10)));
        } catch (Exception e) { return iso.substring(0, 10); }
    }

    private static int dayNumber() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.YEAR) * 366 + c.get(Calendar.DAY_OF_YEAR);
    }

    private static String dayKey() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.YEAR) + "-" + c.get(Calendar.DAY_OF_YEAR);
    }
}
