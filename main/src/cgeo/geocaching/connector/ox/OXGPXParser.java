package cgeo.geocaching.connector.ox;

import cgeo.geocaching.Geocache;
import cgeo.geocaching.files.GPX10Parser;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.annotation.NonNull;

public class OXGPXParser extends GPX10Parser {

    private final boolean isDetailed;

    public OXGPXParser(final int listIdIn, final boolean isDetailed) {
        super(listIdIn);
        this.isDetailed = isDetailed;
    }

    @Override
    protected void afterParsing(final Geocache cache) {
        cache.setUpdated(System.currentTimeMillis());
        if (isDetailed) {
            cache.setDetailedUpdate(cache.getUpdated());
            cache.setDetailed(true);
        }
        removeTitleFromShortDescription(cache);
    }

    /**
     * The short description of OX caches contains "title by owner, type(T/D/Awesomeness)". That is a lot of
     * duplication. Additionally a space between type and (T/D/Awesomeness) is introduced.
     *
     */
    private static void removeTitleFromShortDescription(final @NonNull Geocache cache) {
        cache.setShortDescription(StringUtils.replace(StringUtils.trim(StringUtils.substringAfterLast(cache.getShortDescription(), ",")), "(", " ("));
    }
}
