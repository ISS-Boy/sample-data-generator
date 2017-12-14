/*
 * Copyright 2014 Open mHealth
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

package org.openmhealth.data.generator.service;

import org.openmhealth.data.generator.domain.BoundedRandomVariableContainer;
import org.openmhealth.data.generator.domain.MeasureGenerationRequest;
import org.openmhealth.data.generator.domain.TimestampedValueGroup;
import org.openmhealth.data.generator.domain.DataTrend;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author Emerson Farrugia
 */
@Service
public class TimestampedValueGroupGenerationServiceImpl implements TimestampedValueGroupGenerationService {

    public static final int NIGHT_TIME_START_HOUR = 23;
    public static final int NIGHT_TIME_END_HOUR = 6;


    @Override
    public Iterable<TimestampedValueGroup> generateValueGroups(MeasureGenerationRequest request) {

        // 获取测量时间间隔，生成指数分布
//        ExponentialDistribution interPointDurationDistribution =
//                new ExponentialDistribution(request.getMeanInterPointDuration().getSeconds());
        Duration duration = request.getMeanInterPointDuration();
        // 获取整个时间间隔
        long totalDurationInS = Duration.between(request.getStartDateTime(), request.getEndDateTime()).getSeconds();

        // 获取数据生成的开始时间
        OffsetDateTime effectiveDateTime = request.getStartDateTime();

        // 准备按设置好的时间间隔生成时间点序列
        List<TimestampedValueGroup> timestampedValueGroups = new ArrayList<>();

        do {
            // 设置随机数据对应时间点
            TimestampedValueGroup valueGroup = new TimestampedValueGroup();
            valueGroup.setTimestamp(effectiveDateTime);

            // 如果到期则停止
            if (!effectiveDateTime.isBefore(request.getEndDateTime())) {
                break;
            }

            // 伪造实际数据生成时间点
            effectiveDateTime = effectiveDateTime.plus(duration);

            // 如果夜晚不测量则跳过此时间点
            if (request.isSuppressNightTimeMeasures() != null && request.isSuppressNightTimeMeasures() &&
                    (effectiveDateTime.getHour() >= NIGHT_TIME_START_HOUR ||
                            effectiveDateTime.getHour() < NIGHT_TIME_END_HOUR)) {
                continue;
            }


            if (request.isDaily()) {
                // 遍历每个container的数据
                for (Map.Entry<String, BoundedRandomVariableContainer> containerEntry : request.getContainers().entrySet()) {

                    String key = containerEntry.getKey();
                    BoundedRandomVariableContainer container = containerEntry.getValue();

                    // 根据当前时间生成对应的值
                    double value = container.nextValue(interpolateForTime(container.getTrends(), effectiveDateTime));
                    valueGroup.setValue(key, value);
                }
            } else {
                request.getContainers().forEach((measures, container) -> {
                    DataTrend rule = container.getTrends().get(0);
                    double mean = (rule.getStartValue() + rule.getEndValue()) / 2;
                    valueGroup.setValue(measures, container.nextValue(mean));
                });

            }


            timestampedValueGroups.add(valueGroup);
        }
        while (true);

        return timestampedValueGroups;
    }

    /**
     * @param dataTrends dataTrends for a container
     * @param time       to find a trend in dataTrends match the given time
     * @return mean value for the given time
     */
    private double interpolateForTime(List<DataTrend> dataTrends, OffsetDateTime time) {
        double current = time.get(ChronoField.MINUTE_OF_DAY) / 60d;
        double fraction;
        double mean = 0.0;

        outer:
        for (DataTrend trend : dataTrends) {
            double start = trend.getStartMoment();
            double end = trend.getEndMoment();
            double tmp = 24d - start;// 以此为刻度
            double length = (end + tmp) % 24d;
            double curInRange = (current + tmp) % 24d;
            // 判断时间是否在区间[start,end)内
            if (curInRange >= 0 && curInRange < length) {
                switch (trend.getShape()) {
                    case "linear":
                        fraction = (current + tmp) % 24 / length;
                        mean = trend.getStartValue() + fraction * (trend.getEndValue() - trend.getStartValue());
                        break outer;
                    case "steady":
                        mean = (trend.getStartValue() + trend.getEndValue()) / 2;
                        break;
                    default:
                        break;
                }
            }


        }
        return mean;
    }


}
