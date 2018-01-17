/**
 * Copyright 2015-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazon.android.utils;

import com.amazon.utils.R;
import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.util.Log;
import android.widget.ImageView;

import java.util.Locale;

/**
 * This class contains helper methods for loading images into image views using the Glide library.
 */
public class GlideHelper {

    private static final String TAG = GlideHelper.class.getSimpleName();


    /**
     * Loads an image using Glide from a URL into an image view and cross fades it with the image
     * view's current image.
     *
     * @param context           The activity.
     * @param imageView         The image view to load the image into to.
     * @param url               The URL that points to the image to load.
     * @param crossFadeDuration The duration of the cross-fade in milliseconds.
     */
    public static void loadImageWithCrossFadeTransition(Context context, ImageView imageView,
                                                        String url, final int crossFadeDuration,
                                                        int error) {

        // With this listener we override the onResourceReady of the RequestListener to force
        // the cross fade animation.
        RequestListener crossFadeListener = new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target,
                                       boolean isFirstResource) {

                Log.d(TAG, String.format(Locale.ROOT, "onException(%s, %s, %s, %s)", e, model,
                                         target, isFirstResource), e);
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, String model,
                                           Target<GlideDrawable> target, boolean isFromMemoryCache,
                                           boolean isFirstResource) {

                ImageViewTarget<GlideDrawable> imageTarget
                        = (ImageViewTarget<GlideDrawable>) target;
                Drawable current = imageTarget.getCurrentDrawable();
                if (current != null) {
                    TransitionDrawable transitionDrawable
                            = new TransitionDrawable(new Drawable[]{current, resource});
                    transitionDrawable.setCrossFadeEnabled(true);
                    transitionDrawable.startTransition(crossFadeDuration);
                    imageTarget.setDrawable(transitionDrawable);
                    return true;
                }
                else
                    return false;
            }
        };

        // With the Glide image managing framework, cross fade animations only take place if the
        // image is not already downloaded in cache. In order to have the cross fade animation
        // when the image is in cache, we need to make the following two calls.
        loadImageIntoView(imageView, context, url, new LoggingListener<>(), error,
                          imageView.getDrawable());

        // Adding this second Glide call enables cross-fade transition even if the image is cached.
        createDrawableRequestBuilder(context, url, crossFadeListener, error,
                                     imageView.getDrawable()).crossFade().into(imageView);

    }

    /**
     * Loads an image into an image view using Glide.
     *
     * @param imageView   The image view to use.
     * @param context     The activity to use.
     * @param url         The URL that points to the image to load.
     * @param listener    The request listener to use.
     * @param error       The id of the resource to use as a placeholder if there's an error.
     * @param placeholder The drawable to display as a placeholder.
     */
    public static void loadImageIntoView(ImageView imageView, Context context, String url,
                                         RequestListener<String, GlideDrawable> listener,
                                         int error, Drawable placeholder) {

        createDrawableRequestBuilder(context, url, listener, error, placeholder).into(imageView);
    }

    /**
     * Loads an image into an image view using Glide.
     *
     * @param imageView The image view to use.
     * @param context   The activity to use.
     * @param url       The URL that points to the image to load.
     * @param listener  The request listener to use.
     * @param error     The id of the resource to use as a placeholder if there's an error.
     */
    public static void loadImageIntoView(ImageView imageView, Context context, String url,
                                         RequestListener<String, GlideDrawable> listener,
                                         int error) {

        createDrawableRequestBuilder(context, url, listener, error, null).into(imageView);
    }

    /**
     * Loads an image into a GlideDrawable simple target.
     *
     * @param context      The context to use.
     * @param url          The URL that points to the image to load.
     * @param listener     The request listener to use.
     *                     error.
     * @param simpleTarget The target to load the resource into.
     */
    public static void loadImageIntoSimpleTarget(Context context, String url,
                                                 LoggingListener<String, GlideDrawable> listener,
                                                 SimpleTarget<GlideDrawable> simpleTarget) {

        Glide.with(context)
             .load(url)
             .listener(listener)
             .centerCrop()
             .into(simpleTarget);
    }

    /**
     * Loads an image as a bitmap into a Bitmap simple target.
     *
     * @param context      The context to use.
     * @param url          The URL that points to the image to load.
     * @param listener     The request listener to use.
     * @param error        The id of the resource to use as a placeholder if there's an
     *                     error.
     * @param simpleTarget The target to load the resource into.
     */
    public static void loadImageIntoSimpleTargetBitmap(Context context, String url,
                                                       LoggingListener listener, int error,
                                                       SimpleTarget<Bitmap> simpleTarget) {

        Glide.with(context)
             .load(url)
             .asBitmap()
             .listener(listener)
             .centerCrop()
             .error(error)
             .into(simpleTarget);
    }

    /**
     * Creates a Glide drawable request builder using the given parameters for loading an image
     * into an image view.
     *
     * @param context     The context to use.
     * @param url         The URL that points to the image to load.
     * @param listener    The request listener to use.
     * @param error       The id of the resource to use as a placeholder if there's an
     *                    error.
     * @param placeholder The drawable to display as a placeholder. If null, placeholderId
     *                    will be used.
     * @return The request builder.
     */
    private static DrawableRequestBuilder createDrawableRequestBuilder(Context context, String
            url, RequestListener<String, GlideDrawable> listener, int error, Drawable placeholder) {

        return Glide.with(context)
                    .load(url)
                    .listener(listener)
                    .fitCenter()
                    .placeholder(placeholder)
                    .error(error);
    }

    /**
     * A debug helper class to listen for errors when loading image resources via Glide.
     *
     * @param <T> The type of the input source.
     * @param <R> The type of the resource that will be transcoded from the loaded resource.
     */
    public static class LoggingListener<T, R> implements RequestListener<T, R> {

        @Override
        public boolean onException(Exception e, Object model, Target target, boolean
                isFirstResource) {

            Log.e(TAG, String.format(Locale.ROOT, "onException(%s, %s, %s, %s)", e, model, target,
                                     isFirstResource), e);
            return false;
        }

        @Override
        public boolean onResourceReady(Object resource, Object model, Target target, boolean
                isFromMemoryCache, boolean isFirstResource) {

            return false;
        }
    }

    /**
     * A debug helper class to listen for errors when loading image resources via Glide.
     */
    public static class DrawableListener implements RequestListener<String,GlideDrawable>  {

        @Override
        public boolean onException(Exception e, String model, Target<GlideDrawable> target,
                                   boolean isFirstResource) {
            Log.e(TAG, String.format(Locale.ROOT, "onException(%s, %s, %s, %s)", e, model, target,
                                     isFirstResource), e);
            return false;
        }

        @Override
        public boolean onResourceReady(GlideDrawable resource, String model,
                                       Target<GlideDrawable> target, boolean isFromMemoryCache,
                                       boolean isFirstResource) {

            return false;
        }
    }
}
