package cgeo.geocaching.filter;

import cgeo.geocaching.CgeoApplication;
import cgeo.geocaching.R;
import cgeo.geocaching.enumerations.CacheType;
import cgeo.geocaching.settings.Settings;
import cgeo.geocaching.utils.Log;
import cgeo.geocaching.utils.TextUtils;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

import rx.functions.Action1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.widget.ArrayAdapter;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class FilterUserInterface {

    private static class FactoryEntry {
        @NonNull private final String name;
        @Nullable private final Class<? extends IFilterFactory> filterFactory;

        public FactoryEntry(@NonNull final String name, @Nullable final Class<? extends IFilterFactory> filterFactory) {
            this.name = name;
            this.filterFactory = filterFactory;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private final Activity activity;
    private final ArrayList<FactoryEntry> registry;
    private final Resources res;

    public FilterUserInterface(final Activity activity) {
        this.activity = activity;
        this.res = CgeoApplication.getInstance().getResources();

        registry = new ArrayList<>();
        if (Settings.getCacheType() == CacheType.ALL) {
            register(R.string.caches_filter_type, TypeFilter.Factory.class);
        }
        register(R.string.caches_filter_size, SizeFilter.Factory.class);
        register(R.string.cache_terrain, TerrainFilter.Factory.class);
        register(R.string.cache_difficulty, DifficultyFilter.Factory.class);
        register(R.string.cache_attributes, AttributeFilter.Factory.class);
        register(R.string.cache_status, StateFilter.Factory.class);
        register(R.string.caches_filter_track, TrackablesFilter.class);
        register(R.string.caches_filter_origin, OriginFilter.Factory.class);
        register(R.string.caches_filter_distance, DistanceFilter.Factory.class);
        register(R.string.caches_filter_popularity, PopularityFilter.Factory.class);
        register(R.string.caches_filter_popularity_ratio, PopularityRatioFilter.Factory.class);
        register(R.string.caches_filter_personal_data, PersonalDataFilterFactory.class);
        register(R.string.caches_filter_rating, RatingFilter.class);

        // sort by localized names
        final Collator collator = TextUtils.getCollator();
        Collections.sort(registry, new Comparator<FactoryEntry>() {

            @Override
            public int compare(final FactoryEntry lhs, final FactoryEntry rhs) {
                return collator.compare(lhs.name, rhs.name);
            }
        });

        // reset shall be last
        register(R.string.caches_filter_clear, null);
    }

    private void register(final int resourceId, @Nullable final Class<? extends IFilterFactory> factoryClass) {
        registry.add(new FactoryEntry(res.getString(resourceId), factoryClass));
    }

    public void selectFilter(final Action1<IFilter> runAfterwards) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.caches_filter_title);

        final ArrayAdapter<FactoryEntry> adapter = new ArrayAdapter<>(activity, android.R.layout.select_dialog_item, registry);

        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int itemIndex) {
                final FactoryEntry entry = adapter.getItem(itemIndex);
                // reset?
                final Class<? extends IFilterFactory> filterFactory = entry.filterFactory;
                if (filterFactory == null) {
                    runAfterwards.call(null);
                }
                else {
                    try {
                        final IFilterFactory factoryInstance = filterFactory.newInstance();
                        selectFromFactory(factoryInstance, entry.name, runAfterwards);
                    } catch (final Exception e) {
                        Log.e("selectFilter", e);
                    }
                }
            }
        });

        builder.create().show();
    }

    private void selectFromFactory(@NonNull final IFilterFactory factory, final String menuTitle, final Action1<IFilter> runAfterwards) {
        final List<IFilter> filters = Collections.unmodifiableList(factory.getFilters());
        if (filters.size() == 1) {
            runAfterwards.call(filters.get(0));
            return;
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(menuTitle);

        final ArrayAdapter<IFilter> adapter = new ArrayAdapter<>(activity, android.R.layout.select_dialog_item, filters);
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int item) {
                runAfterwards.call(filters.get(item));
            }
        });

        builder.create().show();
    }

}
