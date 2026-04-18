package com.travel.system.config;

import com.graphhopper.GraphHopper;
import com.graphhopper.config.CHProfile;
import com.graphhopper.config.Profile;
import com.graphhopper.json.Statement;
import com.graphhopper.util.CustomModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
@EnableConfigurationProperties(GraphHopperProperties.class)
public class GraphHopperConfig {
    private static final String PROFILE_CAR = "car";
    private static final String PROFILE_BIKE = "bike";
    private static final String PROFILE_FOOT = "foot";

    @Bean(destroyMethod = "close")
    @ConditionalOnProperty(prefix = "routing.graphhopper", name = "enabled", havingValue = "true")
    public GraphHopper graphHopper(GraphHopperProperties properties) {
        GraphHopper hopper = new GraphHopper();
        hopper.setOSMFile(resolveBackendRelativePath(properties.getOsmFile()));
        hopper.setGraphHopperLocation(resolveBackendRelativePath(properties.getGraphLocation()));

        // GraphHopper 8.x 要求使用 custom 权重。通过 CustomModel 的优先级规则：
        //   1) 大幅惩罚渡口(road_environment == FERRY)，避免步行/骑车穿越水域；
        //   2) 惩罚 destination-only 道路(road_access == DESTINATION)，保证汽车沿公共道路行驶。
        CustomModel carModel = new CustomModel();
        carModel.addToPriority(Statement.If("road_access == DESTINATION", Statement.Op.MULTIPLY, "0.1"));
        carModel.addToPriority(Statement.If("road_environment == FERRY", Statement.Op.MULTIPLY, "0.01"));

        CustomModel bikeModel = new CustomModel();
        bikeModel.addToPriority(Statement.If("road_environment == FERRY", Statement.Op.MULTIPLY, "0.01"));

        CustomModel footModel = new CustomModel();
        footModel.addToPriority(Statement.If("road_environment == FERRY", Statement.Op.MULTIPLY, "0.01"));

        hopper.setProfiles(
                new Profile(PROFILE_CAR).setVehicle("car").setWeighting("custom").setCustomModel(carModel),
                new Profile(PROFILE_BIKE).setVehicle("bike").setWeighting("custom").setCustomModel(bikeModel),
                new Profile(PROFILE_FOOT).setVehicle("foot").setWeighting("custom").setCustomModel(footModel)
        );

        hopper.getCHPreparationHandler().setCHProfiles(
                new CHProfile(PROFILE_CAR),
                new CHProfile(PROFILE_BIKE),
                new CHProfile(PROFILE_FOOT)
        );
        hopper.importOrLoad();
        return hopper;
    }

    private String resolveBackendRelativePath(String rawPath) {
        Path configured = Path.of(rawPath);
        if (configured.isAbsolute()) {
            return configured.normalize().toString();
        }

        Path userDir = Path.of(System.getProperty("user.dir"));
        Path backendDir = userDir;
        if (!Files.exists(userDir.resolve("src"))
                && Files.exists(userDir.resolve("data-structrue-design-backend").resolve("src"))) {
            backendDir = userDir.resolve("data-structrue-design-backend");
        }
        return backendDir.resolve(configured).normalize().toString();
    }
}
