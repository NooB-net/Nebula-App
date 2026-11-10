package com.nebula.app.util;

import com.nebula.app.R;
import com.nebula.app.data.model.SkyEvent;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.ArrayList;
import java.util.List;

public class SampleDataUtil {

    private static void add(List<SkyEvent> l, int id, String name, String type, String label, String isoUtc, String desc) {
        SkyEvent e = new SkyEvent();
        e.setId(id); e.setName(name); e.setType(type); e.setDate(label); e.setDescription(desc);
        e.setTimestamp(parse(isoUtc));
        l.add(e);
    }

    private static long parse(String iso) {
        try {
            SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            f.setTimeZone(TimeZone.getTimeZone("UTC"));
            return f.parse(iso).getTime();
        } catch (Exception ex) { return System.currentTimeMillis(); }
    }

    /** Upcoming events (dates in UTC), sorted soonest first, past ones dropped. */
    public static List<SkyEvent> getSampleEvents() {
        List<SkyEvent> all = new ArrayList<>();
        add(all, 1, "Leonid Meteor Shower", "METEOR", "Nov 17-18, 2026", "2026-11-18T00:00:00Z",
                "Fast, bright meteors from Comet Tempel-Tuttle. A new moon on Nov 9 keeps skies dark, so about 15 meteors an hour are possible after midnight.");
        add(all, 2, "Full Moon (Beaver Moon)", "MOON", "Nov 24, 2026", "2026-11-24T14:53:00Z",
                "The last full moon of autumn rises at sunset and stays up all night. A great target for lunar photography.");
        add(all, 3, "Geminid Meteor Shower", "METEOR", "Dec 13-14, 2026", "2026-12-14T00:00:00Z",
                "The strongest shower of the year, up to 120 meteors an hour under a dark sky. A thin crescent moon sets early and leaves the night dark.");
        add(all, 4, "Full Moon (Cold Moon)", "MOON", "Dec 24, 2026", "2026-12-24T01:28:00Z",
                "The year's final full moon rides high in the sky for the longest nights of the year.");
        add(all, 5, "Quadrantid Meteor Shower", "METEOR", "Jan 3-4, 2027", "2027-01-04T03:00:00Z",
                "A sharp, short peak of up to 80 meteors an hour. The nearly full moon will wash out the fainter ones this year.");
        add(all, 6, "Jupiter at Opposition", "PLANET", "Jan 10, 2027", "2027-01-10T20:00:00Z",
                "Jupiter is closest to Earth and brightest of the year, visible all night. Even binoculars show its four large moons.");
        add(all, 7, "Annular Solar Eclipse", "ECLIPSE", "Feb 6, 2027", "2027-02-06T15:00:00Z",
                "A ring of fire visible across southern South America and parts of the Atlantic and Africa; partial elsewhere in the south.");
        add(all, 8, "Lyrid Meteor Shower", "METEOR", "Apr 22-23, 2027", "2027-04-23T02:00:00Z",
                "Around 18 meteors an hour from Comet Thatcher, radiating from the constellation Lyra.");
        add(all, 9, "Penumbral Lunar Eclipse", "ECLIPSE", "Jul 18, 2027", "2027-07-18T16:00:00Z",
                "The Moon slips through Earth's faint outer shadow. The dimming is subtle but visible across Asia, Australia and Africa.");
        add(all, 10, "Total Solar Eclipse", "ECLIPSE", "Aug 2, 2027", "2027-08-02T10:00:00Z",
                "The longest totality of the century, over 6 minutes, crosses Spain, North Africa and the Middle East. Partial across Europe, Africa and Asia.");
        add(all, 11, "Perseid Meteor Shower", "METEOR", "Aug 12-13, 2027", "2027-08-13T02:00:00Z",
                "Summer's favorite shower, up to 100 meteors an hour, with bright fireballs and long trails.");

        long now = System.currentTimeMillis();
        List<SkyEvent> out = new ArrayList<>();
        for (SkyEvent e : all) if (e.getTimestamp() > now - 86400000L) out.add(e);
        return out;
    }

    public static int thumbFor(String type) {
        if (type == null) return R.drawable.img_orion_nebula;
        switch (type.toUpperCase()) {
            case "METEOR": return R.drawable.img_event_meteor;
            case "ECLIPSE": return R.drawable.img_event_eclipse;
            case "MOON": return R.drawable.img_event_fullmoon;
            default: return R.drawable.img_orion_nebula;
        }
    }

    public static String countdown(long ts) {
        long diff = ts - System.currentTimeMillis();
        if (diff < 0) return "Happening now";
        long days = diff / 86400000L;
        if (days >= 60) return "In " + (days / 30) + " months";
        if (days >= 1) return "In " + days + (days == 1 ? " day" : " days");
        long h = diff / 3600000L;
        return "In " + Math.max(1, h) + " h";
    }
}
