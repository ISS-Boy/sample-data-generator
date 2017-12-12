package org.openmhealth.data.generator.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openmhealth.generator.user.CurrentUserCount;
import org.openmhealth.schema.domain.omh.DataPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by dujijun on 2017/9/25.
 */
@Service
@Primary
@Qualifier("classfication")
@ConditionalOnExpression("'${output.destination}' == 'classification_file'")
public class ClassificationFileSystemDataPointwritingServiceImpl
        implements DataPointWritingService {

    @Value("${output.file.filename:output.json}")
    private String filename;

    @Value("${output.file.append:true}")
    private Boolean append;

    @Value("${output.file.root:.}")
    private String rootDir;

    @Value("${data.header.member-in-user-group:1}")
    private String memberInUserGroup;

    @Autowired
    private ObjectMapper objectMapper;

    public void clearFile(String title) throws IOException {

        if (!append) {
            Files.deleteIfExists(Paths.get( title + "/" + filename));
        }
    }

    @Override
    public long writeDataPoints(Iterable<? extends DataPoint<?>> dataPoints) throws Exception {

            long offset = 0;
            if(dataPoints.iterator().hasNext()) {
            // 获取用户组文件夹
            DataPoint dpTmp = dataPoints.iterator().next();

            String userId = dpTmp.getHeader().getUserId();
            String measureName = dpTmp.getHeader().getBodySchemaId().getName();

            // 检查创建根目录文件夹，用户组文件夹和用户文件夹，并返回用户文件夹path
            String userInGroupPath = getOrCreateUserGroupDir(userId);

            String path = userInGroupPath + "/"
                    + measureName + "-"
                    + filename;
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
        }
            return offset;

    }

    private String getOrCreateUserGroupDir(String userId) throws IOException {
        long userGroupId = CurrentUserCount.currentUser / Long.parseLong(memberInUserGroup);
        String userGroupDir = rootDir + "/UserGroup-" + userGroupId + "/";
        Path rootPath = Paths.get(rootDir);
        Path userGroupRootPath = Paths.get(userGroupDir);
        Path userInGroupPath = Paths.get(userGroupDir + userId + "/");
        if(!Files.exists(rootPath))
            Files.createDirectory(rootPath);

        if(!Files.exists(userGroupRootPath))
            Files.createDirectory(userGroupRootPath);

        if(!Files.exists(userInGroupPath))
            Files.createDirectory(userInGroupPath);

        return userInGroupPath.toString();
    }
}
