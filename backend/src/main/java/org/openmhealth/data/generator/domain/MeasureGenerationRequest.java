/*
 * Copyright 2015 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.openmhealth.data.generator.domain;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;


/**
 * A request to generate measures.
 *
 * @author Emerson Farrugia
 */
public class MeasureGenerationRequest {

    private String generatorName;
    private OffsetDateTime startDateTime;
    private OffsetDateTime endDateTime;
    private Duration meanInterPointDuration;
    private Boolean suppressNightTimeMeasures;
    private Map<String, BoundedRandomVariableContainer> containers = new HashMap<>();
    private Boolean daily;
    /**
     * @return the name of the measure generator to use
     */
    @NotNull
    public String getGeneratorName() {
        return generatorName;
    }

    public void setGeneratorName(String generatorName) {
        this.generatorName = generatorName;
    }

    /**
     * An alias for {@link #setGeneratorName(String)} used by SnakeYAML.
     */
    public void setGenerator(String generatorName) {
        setGeneratorName(generatorName);
    }

    /**
     * @return the earliest date time of the measures to generate. If the measures have time interval time frames,
     * the earliest time interval start time will be no earlier than this date time.
     */
    @NotNull
    public OffsetDateTime getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(OffsetDateTime startDateTime) {
        this.startDateTime = startDateTime;
    }

    /**
     * @return the latest date time of the measures to generate. If the measures have time interval time frames,
     * the latest time interval start time will be no later than this date time.
     */
    @NotNull
    public OffsetDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(OffsetDateTime endDateTime) {
        this.endDateTime = endDateTime;
    }

    /**
     * @return the mean duration between the effective time frames of consecutive measures
     */
    @NotNull
    public Duration getMeanInterPointDuration() {
        return meanInterPointDuration;
    }

    public void setMeanInterPointDuration(Duration meanInterPointDuration) {
        this.meanInterPointDuration = meanInterPointDuration;
    }

    /**
     * @return true if measures having effective time frames at night should be suppressed, or false otherwise
     */
    @NotNull
    public Boolean isSuppressNightTimeMeasures() {
        return suppressNightTimeMeasures;
    }

    public void setSuppressNightTimeMeasures(Boolean suppressNightTimeMeasures) {
        this.suppressNightTimeMeasures = suppressNightTimeMeasures;
    }

    /**
     * @return a map of containers to be generated
     */
    @Valid
    @NotNull
    public Map<String, BoundedRandomVariableContainer> getContainers() {
        return containers;
    }

    public void setContainers(Map<String, BoundedRandomVariableContainer> containers) {
        this.containers = containers;
    }

    public void addTrend(String key, BoundedRandomVariableContainer trend) {
        this.containers.put(key, trend);
    }

    @NotNull
    public Boolean isDaily() {
        return daily;
    }

    public void setDaily(Boolean daily) {
        this.daily = daily;
    }

    @Override
    public String toString() {

        final StringBuilder sb = new StringBuilder("MeasureGenerationRequest{");

        sb.append("generatorName='").append(generatorName).append('\'');
        sb.append(", startDateTime=").append(startDateTime);
        sb.append(", endDateTime=").append(endDateTime);
        sb.append(", meanInterPointDuration=").append(meanInterPointDuration);
        sb.append(", suppressNightTimeMeasures=").append(suppressNightTimeMeasures);
        sb.append(", containers=").append(containers);
        sb.append('}');

        return sb.toString();
    }
}
