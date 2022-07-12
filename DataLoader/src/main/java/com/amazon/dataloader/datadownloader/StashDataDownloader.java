package com.amazon.dataloader.datadownloader;

import android.content.Context;

import com.amazon.android.recipe.Recipe;
import com.amazon.dataloader.R;
import com.amazon.utils.model.Data;

import java.util.List;

public class StashDataDownloader extends ADataDownloader {
    private static final String TAG = StashDataDownloader.class.getSimpleName();

    // Key to locate the URL generator implementation.
    protected static final String URL_GENERATOR_IMPL = "url_generator_impl";
    // Key to locate the URL generator.
    protected static final String URL_GENERATOR_RECIPE = "url_generator";

    private static final String PREFERENCE_TERMS = "ZypeTerms";

    /**
     * {@link AUrlGenerator} instance.
     */
    private final AUrlGenerator urlGenerator;

    /**
     * Constructor for {@link StashDataDownloader}. It initializes the URL generator using
     * the URL generator implementation defined in the configuration.
     *
     * @param context The context.
     * @throws ObjectCreatorException Any exception generated while fetching this instance will be
     *                                wrapped in this exception.
     * @throws DataLoaderException    If there was an error while creating the URL generator.
     */
    public StashDataDownloader(Context context) throws ObjectCreatorException, DataLoaderException {
        super(context);
        try {
            String urlGeneratorClassPath = mConfiguration.getItemAsString(URL_GENERATOR_IMPL);
            this.urlGenerator = UrlGeneratorFactory.createUrlGenerator(mContext, urlGeneratorClassPath);
        }
        catch (UrlGeneratorFactory.UrlGeneratorInitializationFailedException e) {
            throw new DataLoaderException("Exception in initialization of " + "ZypeDataDownloader ", e);
        }
    }

    /**
     * Creates an instance of this class.
     *
     * @param context The context.
     * @return The {@link BasicHttpBasedDataDownloader} instance.
     * @throws ObjectCreatorException Any exception generated while fetching this instance will be
     *                                wrapped in this exception.
     */
    public static ADataDownloader createInstance(Context context) throws ObjectCreatorException {
        try {
            return new StashDataDownloader(context);
        }
        catch (DataLoaderException e) {
            throw new ObjectCreatorException("Exception while creating instance ", e);
        }
    }

    @Override
    protected Data fetchData(Recipe dataLoadRecipe) throws Exception {

        return null;
    }

    /**
     * Returns the configuration file path for this class relative to the assets folder.
     *
     * @param context The application context.
     * @return The path of the config file.
     */
    @Override
    protected String getConfigFilePath(Context context) {
        return mContext.getString(R.string.stash_downloader_config_file_path);
    }
}
