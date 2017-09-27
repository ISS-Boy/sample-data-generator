package org.openmhealth.data.generator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;

/**
 * Created by dujijun on 2017/9/25.
 */
@Service
@Primary
@ConditionalOnExpression("'${output.destination}' == 'multiple-user-file'")
public class MultipleUserFileSystemDataPointWritingServiceImpl
        implements DataPointWritingService {
    @Value("${output.file.filename:output.json}")
    private String filename;

    @Value("${output.file.append:true}")
    private Boolean append;

    @Value("${output.file.root:.}")
    private String rootDir;

    @Autowired
    private ObjectMapper objectMapper;

    public void clearFile(String path) throws IOException {

        if (!append) {
            Files.deleteIfExists(Paths.get(path));
        }
    }

    @Override
    public long writeDataPoints(Iterable<? extends DataPoint<?>> dataPoints) throws Exception {

        long offset = 0;
        String userId = dataPoints.iterator().next().getHeader().getUserId();
        String userRootPath = rootDir + "/" + userId;
        if (!Files.exists(Paths.get(userRootPath)))
            Files.createDirectory(Paths.get(userRootPath));
        String path = userRootPath + "/" + filename;
        clearFile(path);
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(path, true));) {
            for (DataPoint dataPoint : dataPoints) {
                dataPoint.setAdditionalProperty("id", dataPoint.getHeader().getId());
                String valueAsString = objectMapper.writeValueAsString(dataPoint);
                bw.write(valueAsString);
                bw.write("\n");
                offset++;
            }
        }
        return offset;
    }
}
