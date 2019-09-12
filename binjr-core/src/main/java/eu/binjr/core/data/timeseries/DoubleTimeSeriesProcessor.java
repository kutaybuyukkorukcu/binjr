/*
 *    Copyright 2017-2019 Frederic Thevenet
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package eu.binjr.core.data.timeseries;

import eu.binjr.core.data.workspace.TimeSeriesInfo;
import javafx.scene.chart.XYChart;

import java.util.OptionalDouble;

/**
 * This class provides a full implementation of a {@link TimeSeriesProcessor} of {@link Double} values.
 *
 * @author Frederic Thevenet
 */
public class DoubleTimeSeriesProcessor extends TimeSeriesProcessor {

    /**
     * Initializes a new instance of the {@link DoubleTimeSeriesProcessor} class with the provided binding.
     */
    public DoubleTimeSeriesProcessor() {
        super();
    }

    @Override
    public Double computeMinValue() {
        return this.data.stream()
                .mapToDouble(XYChart.Data::getYValue)
                .filter(d ->!Double.isNaN(d))
                .min()
                .orElse(Double.NaN);
    }

    @Override
    public Double computeAverageValue() {
       return this.data.stream()
               .mapToDouble(XYChart.Data::getYValue)
               .filter(d ->!Double.isNaN(d))
               .average()
               .orElse(Double.NaN);
    }

    @Override
    public Double computeMaxValue() {
       return this.data.stream()
               .mapToDouble(XYChart.Data::getYValue)
               .filter(d ->!Double.isNaN(d))
               .max()
               .orElse(Double.NaN);
    }
}
