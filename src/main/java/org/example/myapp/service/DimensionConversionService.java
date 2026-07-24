package org.example.myapp.service;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class DimensionConversionService {

    private static final double INCH_TO_CM = 2.54;
    private static final double CM_TO_INCH = 1 / INCH_TO_CM;

    // Convert inches → centimeters (imperial → metric)
    public double inchesToCm(double inches) {
        return inches * INCH_TO_CM;
    }

    // Convert centimeters → inches (metric → imperial)
    public double cmToInches(double cm) {
        return cm * CM_TO_INCH;
    }

    // Convert full dimension set from imperial → metric
    public ConvertedDimensions toMetric(double widthInches, double heightInches, double lengthInches) {
        return new ConvertedDimensions(
                inchesToCm(widthInches),
                inchesToCm(heightInches),
                inchesToCm(lengthInches)
        );
    }

    // Convert full dimension set from metric → imperial
    public ConvertedDimensions toImperial(double widthCm, double heightCm, double lengthCm) {
        return new ConvertedDimensions(
                cmToInches(widthCm),
                cmToInches(heightCm),
                cmToInches(lengthCm)
        );
    }

    // Simple record to hold converted values
    public static class ConvertedDimensions {
        public final double width;
        public final double height;
        public final double length;

        public ConvertedDimensions(double width, double height, double length) {
            this.width = width;
            this.height = height;
            this.length = length;
        }
    }
}
