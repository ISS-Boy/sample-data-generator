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

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.openmhealth.data.generator.domain.BoundedRandomVariableTrend;
import org.openmhealth.data.generator.domain.MeasureGenerationRequest;
import org.openmhealth.data.generator.domain.TimestampedValueGroup;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoUnit.SECONDS;


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
        ExponentialDistribution interPointDurationDistribution =
                new ExponentialDistribution(request.getMeanInterPointDuration().getSeconds());

        // 获取整个时间间隔
        long totalDurationInS = Duration.between(request.getStartDateTime(), request.getEndDateTime()).getSeconds();

        // 获取数据生成的开始时间
        OffsetDateTime effectiveDateTime = request.getStartDateTime();

        // 准备按设置好的时间间隔生成时间点序列
        List<TimestampedValueGroup> timestampedValueGroups = new ArrayList<>();

        do {
            // 伪造实际数据生成时间点
            effectiveDateTime = effectiveDateTime.plus((long) interPointDurationDistribution.sample(), SECONDS);

            // 如果到期则停止
            if (!effectiveDateTime.isBefore(request.getEndDateTime())) {
                break;
            }

            // 如果夜晚不测量则跳过此时间点
            if (request.isSuppressNightTimeMeasures() != null && request.isSuppressNightTimeMeasures() &&
                    (effectiveDateTime.getHour() >= NIGHT_TIME_START_HOUR ||
                            effectiveDateTime.getHour() < NIGHT_TIME_END_HOUR)) {
                continue;
            }

            // 设置随机数据对应时间点
            TimestampedValueGroup valueGroup = new TimestampedValueGroup();
            valueGroup.setTimestamp(effectiveDateTime);

            // 获取此时间点在数据总时间段中的进度
            double trendProgressFraction = (double)
                    Duration.between(request.getStartDateTime(), effectiveDateTime).getSeconds() / totalDurationInS;

            // 遍历每个trend的数据
            for (Map.Entry<String, BoundedRandomVariableTrend> trendEntry : request.getTrends().entrySet()) {

                // 按设定趋势通过时间进度预估生成值
                String key = trendEntry.getKey();
                BoundedRandomVariableTrend trend = trendEntry.getValue();

                double value = trend.nextValue(trendProgressFraction);
                valueGroup.setValue(key, value);
            }

            timestampedValueGroups.add(valueGroup);
        }
        while (true);

        return timestampedValueGroups;
    }
}
