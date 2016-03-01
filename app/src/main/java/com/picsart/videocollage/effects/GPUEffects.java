package com.picsart.videocollage.effects;

import java.util.LinkedList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImageColorInvertFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageEmbossFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.GPUImagePosterizeFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSepiaFilter;

/**
 * Created by Tigran on 10/8/15.
 */
public class GPUEffects {

    public enum FilterType {
        NONE, GRAYSCALE, SEPIA, EMBOSS, POSTERIZE, INVERT
    }

    public static class FilterList {
        public List<String> names = new LinkedList();
        public List<FilterType> filters = new LinkedList();

        public void addFilter(final String name, final FilterType filter) {
            names.add(name);
            filters.add(filter);
        }
    }

    public static GPUImageFilter createFilterForType(final FilterType type) {
        switch (type) {
            case NONE:
                return new GPUImageFilter();
            case INVERT:
                return new GPUImageColorInvertFilter();
            case GRAYSCALE:
                return new GPUImageGrayscaleFilter();
            case SEPIA:
                return new GPUImageSepiaFilter();
            case EMBOSS:
                return new GPUImageEmbossFilter();
            case POSTERIZE:
                return new GPUImagePosterizeFilter();
            default:
                throw new IllegalStateException("No filter of that type!");
        }
    }

}
