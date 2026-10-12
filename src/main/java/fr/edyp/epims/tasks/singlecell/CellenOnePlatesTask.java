package fr.edyp.epims.tasks.singlecell;

import fr.edyp.epims.dataaccess.AbstractAuthenticateDatabaseTask;
import fr.edyp.epims.dataaccess.AbstractDatabaseCallback;
import fr.edyp.epims.dataaccess.DataManager;
import fr.edyp.epims.dataaccess.TaskError;
import fr.edyp.epims.dataaccess.TaskInfo;
import fr.edyp.epims.dataaccess.TokenManager;
import fr.edyp.epims.json.PlateJson;
import fr.edyp.epims.json.StudyJson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class CellenOnePlatesTask extends AbstractAuthenticateDatabaseTask {

    private final String url;
    private final StudyJson study;
    private final List<PlateJson> plates;

    public CellenOnePlatesTask(AbstractDatabaseCallback callback, StudyJson study, List<PlateJson> plates) {
        super(callback, new TaskInfo("Load Plates of a Study " + study.getTitle(), false, null), TokenManager.TOKEN_EPIMS_SERVER);
        this.url = DataManager.getServerURL() + "/api/cellenoneplates/";
        this.study = study;
        this.plates = plates;
    }

    @Override
    public boolean fetchSecuredData(HttpEntity<String> entity, RestTemplate restTemplate) {
        try {
            ResponseEntity<PlateJson[]> response = restTemplate.exchange(url + study.getId(), HttpMethod.GET, entity, PlateJson[].class);
            PlateJson[] result = response.getBody();
            if (result != null) {
                plates.addAll(List.of(result));
            }
        } catch (Exception e) {
            m_taskError = new TaskError(e);
            return false;
        }
        return true;
    }

    @Override
    public boolean needToFetch() {
        return true;
    }
}