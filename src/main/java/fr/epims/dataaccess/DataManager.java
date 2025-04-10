/*
 * Copyright (C) 2021
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the CeCILL FREE SOFTWARE LICENSE AGREEMENT
 * ; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * CeCILL License V2.1 for more details.
 *
 * You should have received a copy of the CeCILL License
 * along with this program; If not, see <http://www.cecill.info/licences/Licence_CeCILL_V2.1-en.html>.
 */

package fr.epims.dataaccess;

import fr.edyp.epims.json.*;
import fr.epims.tasks.*;
import fr.epims.tasks.analyses.LoadPriceListTask;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.util.*;

/**
 *
 * This class contains all data loaded from the server. The data is automatically updated
 * if needed when the user does some actions (add study, add sample, refresh...)
 *
 * @author JM235353
 *
 */
public class DataManager {

    public static final int CLIENT_VERSION = 1; // Client and server version must be the same

    public enum ActionTypeEnum {
        UPDATE,
        CREATE
    };

    private static Object MUTEX = new Object();

    private static boolean m_dataLoaded = false;

    // Server URL and its Database version
    private static String m_serverURL = null;
    private static DatabaseVersionJson m_databaseVersion = null;

    // Analyses Server URL
    private static String m_analysesLoggedUser = null;
    private static String m_analysesFullNameUser = null;
    private static String m_analysesServerURL = null;

    // user logged
    private static String m_loggedUser = null;
    private static String m_loggedUserRole = "";

    // notify to be done
    private static HashSet<Class> m_notifyToBeDoneSet = new HashSet();

    // Loaded data
    private static FtpConfigurationJson m_ftpConfiguration = null;

    private static ArrayList<ProgramJson> m_programs = null;
    private static ArrayList<ProjectJson> m_projects = null;
    private static ArrayList<StudyJson> m_studies = null;

    private static ArrayList<ActorJson> m_actors = null;
    private static ArrayList<ContactJson> m_contacts = null;
    private static ArrayList<CompanyJson> m_companies = null;
    private static ArrayList<String> m_acquisitionTypes = null;

    private static ArrayList<InstrumentJson> m_spectrometers = null;

    private static ArrayList<SampleSpeciesJson> m_sampleSpecies = null;
    private static ArrayList<SampleTypeJson> m_sampleTypes = null;

    private static ArrayList<AnalysisPriceListJson> m_analysisPriceListArray = null;
    private static HashMap<Integer, AnalysisPriceListJson> m_analysisPriceListMap = new HashMap<>();

    // Fast Access Data
    private static HashMap<String, ActorJson> m_actorMap = new HashMap<>();
    private static HashMap<Integer, ContactJson> m_contactMap = new HashMap<>();
    private static HashMap<Integer, ActorJson> m_contactToActorMap = new HashMap<>();

    private static HashMap<Integer, ProgramJson> m_programMap = new HashMap<>();
    private static HashMap<Integer, ProjectJson> m_projectMap = new HashMap<>();
    private static HashMap<Integer, StudyJson> m_studyMap = new HashMap<>();
    private static HashMap<String, CompanyJson> m_companyMap = new HashMap<>();
    private static HashMap<Integer, InstrumentJson> m_instrumentsKeyMap = new HashMap<>();
    private static HashMap<String, InstrumentJson> m_instrumentsNameMap = new HashMap<>();

    private static HashMap<Integer, SampleSpeciesJson> m_sampleSpeciesMap = new HashMap<>();
    private static HashMap<Integer, SampleTypeJson> m_sampleTypesMap = new HashMap<>();

    private static Stack<DataAvailableCallback> m_callbacks = new Stack<>();
    private static Stack<DataAvailableCallback> m_everLastingcallbacks = new Stack<>();

    private static HashMap<Class, HashSet<DataManagerListener>> m_listeners = new HashMap<>();

    private static final String[] m_roles = { "user", "robot user", "admin user", "admin" };

    public static DatabaseVersionJson getDatabaseVersion() {
        return m_databaseVersion;
    }

    public static String getServerURL() {
        return m_serverURL;
    }

    public static void setAnalysesServerURL(String analysesServerURL) {
        m_analysesServerURL = analysesServerURL;
    }
    public static String getAnalysesServerURL() {
        return m_analysesServerURL;
    }


    public static void clearAllData() {

        m_serverURL = null;
        m_databaseVersion = null;

        m_ftpConfiguration = null;

        m_programs = null;
        m_projects = null;
        m_studies = null;

        m_actors = null;
        m_contacts = null;
        m_companies = null;
        m_acquisitionTypes = null;

        m_spectrometers = null;

        m_sampleSpecies = null;
        m_sampleTypes = null;

        m_actorMap.clear();
        m_contactMap.clear();
        m_contactToActorMap.clear();

        m_programMap.clear();
        m_projectMap.clear();
        m_studyMap.clear();
        m_companyMap.clear();
        m_instrumentsKeyMap.clear();
        m_instrumentsNameMap.clear();

        m_sampleSpeciesMap.clear();
        m_sampleTypesMap.clear();

        m_dataLoaded = false;

        synchronized (MUTEX) {
            m_callbacks.clear();
            for (DataAvailableCallback callback : m_everLastingcallbacks) {
                m_callbacks.add(callback);
            }
        }
    }

    public static boolean updateData(String specificClassName, DatabaseVersionJson databaseServerVersion) {
        LoggerFactory.getLogger("Epims.Client").debug("Update data for "+specificClassName);
        HashMap<String, Integer> serverVersions = databaseServerVersion.getVersions();
        HashMap<String, Integer> versions = m_databaseVersion.getVersions();

        ArrayList<String> dataToUpdate = new ArrayList();

        for (String className : versions.keySet()) {

            if ((specificClassName != null) && (!specificClassName.equals(className))) {
                continue;
            }
            if ((specificClassName == null) && (className.equals(RobotDataJson.class.getSimpleName()))) {
                //JPM.WART : in general case, we do not take in account RobotDataJson.class
            }

            Integer version = versions.get(className);

            Integer serverVersion = serverVersions.get(className);
            if ((serverVersion == null) || (serverVersion != version)) { // serverVersion should never be null
                dataToUpdate.add(className);
            }
        }

        boolean needUpdate = ! dataToUpdate.isEmpty();

        if (needUpdate) {
            boolean checkAllDataLoadedAtOnce = false;
            for (String className : dataToUpdate) {

                if (className.equals(CompanyJson.class.getSimpleName())) {
                    m_companies = null;
                    loadCompanies(true);
                } else if (className.equals(ProgramJson.class.getSimpleName())) {
                    m_programs = null;
                    m_programMap.clear();
                    loadPrograms(true);
                } else if (className.equals(ProjectJson.class.getSimpleName())) {
                    m_projects = null;
                    m_projectMap.clear();
                    loadProjects(true);
                } else if (className.equals(StudyJson.class.getSimpleName())) {
                    m_studies = null;
                    m_studyMap.clear();
                    loadStudies(true);
                } else if (className.equals(ActorJson.class.getSimpleName())) {
                    m_actors = null;
                    m_actorMap.clear();
                    m_contactToActorMap.clear();
                    loadActors(true);
                } else if (className.equals(ContactJson.class.getSimpleName())) {
                    m_contacts = null;
                    m_contactMap.clear();
                    loadContacts(true);
                } else if (className.equals(InstrumentJson.class.getSimpleName())) {
                    m_spectrometers = null;
                    m_instrumentsKeyMap.clear();
                    m_instrumentsNameMap.clear();
                    loadInstruments(true);
                } else if (className.equals(SampleSpeciesJson.class.getSimpleName())) {
                    m_sampleSpecies = null;
                    m_sampleSpeciesMap.clear();
                    loadSampleSpecies(true);
                } else if (className.equals(SampleTypeJson.class.getSimpleName())) {
                    m_sampleTypes = null;
                    m_sampleTypesMap.clear();
                    loadSampleTypes(true);
                }  else if (className.equals(AnalysisPriceListJson.class.getSimpleName())) {
                    // data are not read now
                    m_analysisPriceListArray = null;
                    m_analysisPriceListMap.clear();
                    loadAnalysisPriceList(true);
                } else if (className.equals(RobotDataJson.class.getSimpleName())) {
                    // data are not read now
                    checkAllDataLoadedAtOnce = true;
                }

            }
            m_dataLoaded = false;


            synchronized (MUTEX) {
                m_callbacks.clear();
                for (DataAvailableCallback callback : m_everLastingcallbacks) {
                    m_callbacks.add(callback);
                }
            }

            if (checkAllDataLoadedAtOnce) {
                checkAllDataLoaded();
            }


        }

        return needUpdate;

    }

    public static void preloadData(String serverURL, DatabaseVersionJson databaseVersion) {

        m_serverURL = serverURL;
        m_databaseVersion = databaseVersion;

        loadActors(false);
        loadContacts(false);
        loadCompanies(false);
        loadInstruments(false);
        loadAcquisitionTypes();
        loadSampleSpecies(false);
        loadSampleTypes(false);
    }

    public static void loadAllSecuredData() {
        LoggerFactory.getLogger("Epims.Client").info("Load data for FTP ... ");
        loadFTPSettings();
        LoggerFactory.getLogger("Epims.Client").info("Programs... ");
        loadPrograms(false);
        LoggerFactory.getLogger("Epims.Client").info("Projects... ");
        loadProjects(false);
        LoggerFactory.getLogger("Epims.Client").info("Studies... ");
        loadStudies(false);
        LoggerFactory.getLogger("Epims.Client").info("Analysis Prices... ");
        loadAnalysisPriceList(false);
    }

    public static void addListener(Class c, DataManagerListener listener ) {
        HashSet<DataManagerListener> listeners = m_listeners.get(c);
        if (listeners == null) {
            listeners = new HashSet<>();
            m_listeners.put(c, listeners);
        }
        listeners.add(listener);
    }

    public static void notifyListeners(Class c, Object o, ActionTypeEnum actionType) {
        HashSet<DataManagerListener> listeners = m_listeners.get(c);
        if (listeners == null) {
            return;
        }
        for (DataManagerListener listener : listeners) {
            listener.update(c, o, actionType);
        }
    }


    public static void dataAvailable(DataAvailableCallback callback, boolean everlasting) {
        dataAvailable(callback, everlasting, false);
    }
    public static void dataAvailable(DataAvailableCallback callback, boolean everlasting, boolean prioritary) {

            if (m_dataLoaded) {
                callback.dataAvailable();
            } else {

                synchronized (MUTEX) {
                    if (prioritary) {
                        m_callbacks.insertElementAt(callback, 0);
                    } else {
                        m_callbacks.add(callback);
                    }
                    if (everlasting) {
                        m_everLastingcallbacks.add(callback);
                    }
                }

            }
    }

    public static FtpConfigurationJson getFtpConfiguration() {
        return m_ftpConfiguration;
    }

    public static FtpConfigurationJson getFtpConfigurationForStudy(StudyJson s) {

        String studyNomenclature = s.getNomenclatureTitle();
        String projectNomenclature = "_UNCLASS_";
        String programNomenclature = "_UNCLASS_";

        if (s.getProjectId() != -1) {
            ProjectJson project = DataManager.getProject(s.getProjectId());
            projectNomenclature = project.getNomenclatureTitle();

            if (project.getProgramId()!= -1) {
                ProgramJson program = DataManager.getProgram(project.getProgramId());
                programNomenclature = program.getNomenclatureTitle();
            }
        }

        String[] subDirs = {programNomenclature, projectNomenclature, studyNomenclature};


        FtpConfigurationJson ftpConfiguration = new FtpConfigurationJson(m_ftpConfiguration.getHost(), m_ftpConfiguration.getLogin(), m_ftpConfiguration.getPassword(),
                m_ftpConfiguration.getStartPath(), subDirs);

        return ftpConfiguration;
    }

    public static void updateProgram(ProgramJson program) {
        for (int i=0;i<m_programs.size();i++) {
            if (m_programs.get(i).getId() == program.getId()) {
                m_programs.set(i, program);
                break;
            }
        }
        m_programMap.put(program.getId(), program);
        notifyListeners(ProgramJson.class, program, ActionTypeEnum.UPDATE);
    }

    public static void addProgram(ProgramJson program) {
        m_programs.add(program);
        m_programMap.put(program.getId(), program);

        Collections.sort(m_programs);
    }



    public static void updateProject(ProjectJson project) {
        for (int i=0;i<m_projects.size();i++) {
            if (m_projects.get(i).getId() == project.getId()) {
                m_projects.set(i, project);
                break;
            }
        }
        m_projectMap.put(project.getId(), project);
        notifyListeners(ProjectJson.class, project, ActionTypeEnum.UPDATE);
    }

    public static void createProgram(ProgramJson programJson) {
        m_programs.add(programJson);
        Collections.sort(m_programs);
        m_programMap.put(programJson.getId(), programJson);

        notifyListeners(ProgramJson.class, programJson, ActionTypeEnum.CREATE);
    }

    public static void createProject(ProjectJson projectJson) {
        m_projects.add(projectJson);
        Collections.sort(m_projects);
        m_projectMap.put(projectJson.getId(), projectJson);

        int programId = projectJson.getProgramId();
        ProgramJson programJson = m_programMap.get(programId);
        programJson.addProject(projectJson);

        notifyListeners(ProjectJson.class, projectJson, ActionTypeEnum.CREATE);
    }

    public static void createStudy(StudyJson studyJson) {
        m_studies.add(studyJson);
        Collections.sort(m_studies);
        m_studyMap.put(studyJson.getId(), studyJson);

        int projectId = studyJson.getProjectId();
        ProjectJson projectJson = m_projectMap.get(projectId);
        projectJson.addStudy(studyJson);

        notifyListeners(StudyJson.class, studyJson, ActionTypeEnum.CREATE);
    }




    public static void updateStudy(StudyJson study) {
        for (int i=0;i<m_studies.size();i++) {
            if (m_studies.get(i).getId() == study.getId()) {
                m_studies.set(i, study);
                break;
            }
        }
        m_studyMap.put(study.getId(), study);
        notifyListeners(StudyJson.class, study, ActionTypeEnum.UPDATE);
    }

    public static void setAnalysesLoggedUser(String analysesLoggedUser) {
        m_analysesLoggedUser = analysesLoggedUser;
    }
    public static String getAnalysesLoggedUser() {
        return m_analysesLoggedUser;
    }

    public static void setAnalysesFullNameUser(String analysesFullNameUser) {
        m_analysesFullNameUser = analysesFullNameUser;
    }
    public static String getAnalysesFullNameUser() {
        return m_analysesFullNameUser;
    }

    public static void reinitLoggedUser() {
        m_loggedUser = null;
        m_loggedUserRole = null;
    }
    public static void setLoggedUser(String loggedUser) {
        m_loggedUser = loggedUser;
        m_loggedUserRole = getActor(loggedUser).getRole();
    }
    public static String getLoggedUser() {
        return m_loggedUser;
    }
    public static boolean isAdmin() {
        return m_loggedUserRole.equals("admin");
    }
    public static boolean isAdminUser() {
        return m_loggedUserRole.equals("admin user") || isAdmin();
    }
    public static boolean isRobotUser() {
        return m_loggedUserRole.equals("robot user") || isAdminUser();
    }

    public static String getRoleTitle() {
        if (isAdmin()) {
            return "Admin";
        }
        if (isAdminUser()) {
            return "Admin User";
        }
        if (isRobotUser()) {
            return "Robot User";
        }
        return "User";
    }

    public static String[] getRoles() {
        return m_roles;
    }

    public static List<AnalysisPriceListJson> getAnalysisPriceListJsonArray() {
        return m_analysisPriceListArray;
    }

    public static AnalysisPriceListJson getAnalysisPriceListJson(Integer id) {
        return m_analysisPriceListMap.get(id);
    }


    public static AnalysisPriceListJson getLastAnalysisPriceListJson() {
        if (m_analysisPriceListArray == null) {
            return null;
        }
        if (m_analysisPriceListArray.isEmpty()) {
            return null;
        }

        return m_analysisPriceListArray.get(m_analysisPriceListArray.size()-1);
    }


    public static InstrumentJson getInstrument(int instrumentId) {
        return m_instrumentsKeyMap.get(instrumentId);
    }
    public static ArrayList<InstrumentJson> getSpectrometers() {
        return m_spectrometers;
    }

    public static HashMap<Integer, InstrumentJson> getInstrumentMap() {
        return m_instrumentsKeyMap;
    }

    public static InstrumentJson getInstrument(String name) {
        return m_instrumentsNameMap.get(name);
    }


    public static void instrumentAdded(InstrumentJson instrument) {
        m_instrumentsKeyMap.put(instrument.getId(), instrument);
        m_instrumentsNameMap.put(instrument.getName(), instrument);

        if (instrument.getIsSpectrometer()) {
            m_spectrometers.add(instrument);
            Collections.sort(m_spectrometers);
        }

    }

    public static void instrumentModified(InstrumentJson instrument) {

        InstrumentJson previousInstrument = m_instrumentsKeyMap.remove(instrument.getId());
        if (previousInstrument.getIsSpectrometer()) {
            m_spectrometers.remove(previousInstrument);
            m_spectrometers.add(instrument);
            Collections.sort(m_spectrometers);
        }
        m_instrumentsKeyMap.put(instrument.getId(), instrument);
        m_instrumentsNameMap.put(instrument.getName(), instrument);
    }

    public static void priceListModified(ArrayList<AnalysisPriceListJson> priceListArray) {

        m_analysisPriceListArray = priceListArray;
        m_analysisPriceListMap.clear();
        for (AnalysisPriceListJson priceList : m_analysisPriceListArray) {
            m_analysisPriceListMap.put(priceList.getId(), priceList);
        }
    }


    public static SampleSpeciesJson getSampleSpecies(int id) {
        return m_sampleSpeciesMap.get(id);
    }
    public static ArrayList<SampleSpeciesJson> getSampleSpecies() {
        return m_sampleSpecies;
    }

    public static ArrayList<SampleSpeciesJson> getRestrictedSampleSpecies() {
        String[] mainSpecies = { "Homo sapiens", "Arabidopsis Thaliana", "Mus musculus", "Rat" };
        ArrayList<SampleSpeciesJson> allSpecies = getSampleSpecies();

        ArrayList<SampleSpeciesJson> mainSampleSpecies = new ArrayList<>();
        for (SampleSpeciesJson sampleSpecies : allSpecies) {
            String name = sampleSpecies.getName();
            for (String curName : mainSpecies) {
                if (name.equals(curName)) {
                    mainSampleSpecies.add(sampleSpecies);
                    break;
                }
            }
        }
        return mainSampleSpecies;
    }

    public static ArrayList<SampleSpeciesJson> getSampleSpeciesWithoutPresents(ArrayList<SampleSpeciesJson> alreadyPresents) {

        ArrayList<SampleSpeciesJson> elements = new ArrayList<>();
        for (SampleSpeciesJson sampleSpeciesJson : m_sampleSpecies) {
            if (! alreadyPresents.contains(sampleSpeciesJson)) {
                elements.add(sampleSpeciesJson);
            }
        }

        Collections.sort(elements);

        return elements;
    }


    public static void sampleSpeciesAdded(SampleSpeciesJson sampleSpeciesJson) {
        m_sampleSpeciesMap.put(sampleSpeciesJson.getId(), sampleSpeciesJson);
        m_sampleSpecies.add(sampleSpeciesJson);

        Collections.sort(m_sampleTypes);
    }

    public static SampleTypeJson getSampleType(int id) {
        return m_sampleTypesMap.get(id);
    }
    public static ArrayList<SampleTypeJson> getSampleTypes() {
        return m_sampleTypes;
    }

    public static ArrayList<SampleTypeJson> getRestrictedSampleTypes() {
        String[] mainTypes = { "Protéines", "Cellule", "Peptides", "Fluide biologique" };
        ArrayList<SampleTypeJson> alltypes = getSampleTypes();

        ArrayList<SampleTypeJson> mainSampleTypes = new ArrayList<>();
        for (SampleTypeJson sampleType : alltypes) {
            String name = sampleType.getName();
            for (String curName : mainTypes) {
                if (name.equals(curName)) {
                    mainSampleTypes.add(sampleType);
                    break;
                }
            }
        }
        return mainSampleTypes;
    }

    public static ArrayList<SampleTypeJson> getSampleTypesWithoutPresents(ArrayList<SampleTypeJson> alreadyPresents) {

        ArrayList<SampleTypeJson> elements = new ArrayList<>();
        for (SampleTypeJson sampleType : m_sampleTypes) {
            if (! alreadyPresents.contains(sampleType)) {
                elements.add(sampleType);
            }
        }

        Collections.sort(elements);

        return elements;
    }

    public static void sampleTypeAdded(SampleTypeJson sampleTypeJson) {
        m_sampleTypesMap.put(sampleTypeJson.getId(), sampleTypeJson);
        m_sampleTypes.add(sampleTypeJson);

        Collections.sort(m_sampleTypes);
    }

    public static ArrayList<ProgramJson > getPrograms() {
        return m_programs;
    }

    public static ArrayList<String> getAcquisitionTypes() {
        return m_acquisitionTypes;
    }

    public static ArrayList<ActorJson> getActors() {
        return m_actors;
    }

    public static ArrayList<String> getActorsKeys() {
        ArrayList<String> actorKeys = new ArrayList<>();
        for (ActorJson actor : m_actors) {
            actorKeys.add(actor.getLogin());
        }
        return actorKeys;

    }

    public static ActorJson getActor(String actorKey) {
        return m_actorMap.get(actorKey);
    }

    public static void contactAdded(ContactJson contactJson) {
        m_contactMap.put(contactJson.getId(), contactJson);
        m_contacts.add(contactJson);

        Collections.sort(m_contacts);
    }

    public static void contactModified(ContactJson contactJson) {
        ContactJson previousContact =  m_contactMap.remove(contactJson.getId());
        m_contacts.remove(previousContact);

        m_contactMap.put(contactJson.getId(), contactJson);
        m_contacts.add(contactJson);

        Collections.sort(m_contacts);
    }

    public static void actorAdded(ActorJson actor) {
        ContactJson contactJson = actor.getContact();
        m_actorMap.put(actor.getLogin(), actor);
        m_contactMap.put(contactJson.getId(), contactJson);
        m_contactToActorMap.put(contactJson.getId(), actor);
        m_actors.add(actor);
        m_contacts.add(contactJson);

        Collections.sort(m_actors);
        Collections.sort(m_contacts);
    }

    public static void actorModified(ActorJson actor) {
        ActorJson previousActor = m_actorMap.remove(actor.getLogin());
        ContactJson previousContact =  m_contactMap.remove(actor.getContact());
        m_actors.remove(previousActor);
        m_contacts.remove(previousContact);

        ContactJson contactJson = actor.getContact();
        m_actorMap.put(actor.getLogin(), actor);
        m_contactMap.put(contactJson.getId(), contactJson);
        m_contactToActorMap.put(contactJson.getId(), actor);
        m_actors.add(actor);
        m_contacts.add(contactJson);

        Collections.sort(m_actors);
        Collections.sort(m_contacts);
    }

    public static ContactJson getContactFromActorKey(String actorKey) {
        if (actorKey == null) {
            return null;
        }
        return m_actorMap.get(actorKey).getContact();
    }

    public static String getNameFromActorKey(String actorKey) {
        if (actorKey == null) {
            return "";
        }
        ActorJson actorJson = DataManager.getActor(actorKey);
        if (actorJson == null) {
            return "";
        }
        ContactJson c = actorJson.getContact();
        if (c == null) {
            return actorJson.getLogin();
        }
        return c.getFirstName() + " " + c.getLastName();
    }

    public static String getLastThenFirstNameFromActorKey(String actorKey) {
        if (actorKey == null) {
            return "";
        }
        ActorJson actor = DataManager.getActor(actorKey);
        if (actor == null) {
            return "";
        }
        ContactJson c = actor.getContact();
        return c.getLastName() + " " + c.getFirstName();
    }

    public static ArrayList<ProjectJson> getProjects(List<Integer> projectsKey) {
        ArrayList<ProjectJson> list = new ArrayList<>(projectsKey.size());
        for (Integer key : projectsKey) {
            list.add(m_projectMap.get(key));
        }

        Collections.sort(list);

        return list;
    }

    public static ProgramJson getProgram(Integer id) {
        return m_programMap.get(id);
    }

    public static ProjectJson getProject(Integer id) {
        return m_projectMap.get(id);
    }

    public static StudyJson getStudy(Integer studyId) {
        return m_studyMap.get(studyId);
    }

    public static StudyJson getStudyByNomenclature(String nomenclature) {
        for (StudyJson s : m_studyMap.values()) {
            if (s.getNomenclatureTitle().equals(nomenclature)) {
                return s;
            }
        }
        return null;
    }

    public static ArrayList<StudyJson> getStudies(List<Integer> studiesKey) {
        ArrayList<StudyJson> list = new ArrayList<>(studiesKey.size());
        for (Integer key : studiesKey) {
            list.add(m_studyMap.get(key));
        }

        Collections.sort(list);

        return list;
    }

    public static ArrayList<StudyJson> getArchivableStudies() {

        ArrayList<StudyJson> list = new ArrayList<>();
        for (StudyJson studyJson : m_studyMap.values()) {
            if (studyJson.isArchivableStatus()) {
                list.add(studyJson);
            }
        }

        Collections.sort(list);

        return list;
    }



    public static ArrayList<ContactJson> getContacts(ArrayList<Integer> contactsKey) {
        ArrayList<ContactJson> list = new ArrayList<>(contactsKey.size());
        for (Integer key : contactsKey) {
            list.add(m_contactMap.get(key));
        }

        Collections.sort(list);

        return list;
    }

    public static ActorJson getActorFromContactId(int contactId) {
        return m_contactToActorMap.get(contactId);
    }


    public static ArrayList<ContactJson> getContacts() {
        return m_contacts;
    }

    public static ArrayList<ContactJson> getAllContactsWithoutPresents(ArrayList<ContactJson> alreadyPresentContacts) {

        ArrayList<ContactJson> contacts = new ArrayList<>();
        for (ContactJson contact : m_contacts) {
            if (! alreadyPresentContacts.contains(contact)) {
                contacts.add(contact);
            }
        }

        Collections.sort(contacts);

        return contacts;
    }

    public static ArrayList<ContactJson> getAllContactsOfActorWithoutPresents(ArrayList<ContactJson> alreadyPresentContacts) {
        ArrayList<ContactJson> contacts = new ArrayList<>();
        for (ActorJson actor : m_actors) {
            ContactJson c = actor.getContact();
            if (! alreadyPresentContacts.contains(c)) {
                contacts.add(c);
            }
        }

        return contacts;
    }


    public static ArrayList<ContactJson> getContactsFromActorsKeys(ArrayList<String> actorsKey) {
        ArrayList<ContactJson> list = new ArrayList<>(actorsKey.size());
        for (String actorKey : actorsKey) {
            list.add(m_actorMap.get(actorKey).getContact());
        }

        Collections.sort(list);

        return list;
    }

    public static ArrayList<ActorJson> getActors(ArrayList<String> actorsKey) {
        ArrayList<ActorJson> list = new ArrayList<>(actorsKey.size());
        for (String actorKey : actorsKey) {
            list.add(m_actorMap.get(actorKey));
        }
        return list;
    }

    public static boolean checkOwner(StudyJson study) {
        boolean ok = checkOwner(study.getActorKey(), study.getActorsKey());
        if (!ok) {
            ProjectJson project = DataManager.getProject(study.getProjectId());
            ok = checkOwner(project);
        }
        return ok;
    }

    public static boolean checkOwner(ProjectJson project) {
        if (project == null) {
            return isAdminUser();
        }
        boolean ok = DataManager.checkOwner(project.getActorKey(), project.getActorsKey());
        if (!ok) {
            ProgramJson program = DataManager.getProgram(project.getProgramId());
            if (program != null) {
                ok = DataManager.checkOwner(program);
            }
        }
        return ok;
    }

    public static boolean checkOwner(ProgramJson program) {
        if (program == null) {
            return isAdminUser();
        }
        return DataManager.checkOwner(program.getResponsible(), program.getActorsKey());
    }

    public static boolean checkOwner(String actorKey, ArrayList<String> membersList) {
        if (m_loggedUser == null) {
            return false;
        }
        if (isAdminUser()) {
            // actions on program, project, studies are always allowed to admin and admin user
            return true;
        }

        if (actorKey == null) {
            return false;
        }
        if (m_loggedUser.equals(actorKey)) {
            return true;
        }
        if (membersList == null) {
            return false;
        }
        for (String memberKey: membersList) {
            if (m_loggedUser.equals(memberKey)) {
                return true;
            }
        }
        return false;
    }

    public static CompanyJson getCompany(ContactJson contactJson) {
        String companyKey = contactJson.getCompany();
        if (companyKey == null) {
            return null;
        } else {
            return m_companyMap.get(companyKey);
        }

    }

    public static CompanyJson getCompany(String companyKey) {
        return m_companyMap.get(companyKey);

    }

    public static ArrayList<CompanyJson> getCompanies() {
        return m_companies;
    }

    public static void companyAdded(CompanyJson company) {
        m_companies.add(company);
        Collections.sort(m_companies);
        m_companyMap.put(company.getName(), company);

        notifyListeners(CompanyJson.class, company, ActionTypeEnum.CREATE);
    }

    public static void companyModified(CompanyJson company) {

        CompanyJson previousCompany = m_companyMap.get(company.getName());
        m_companies.remove(previousCompany);
        m_companies.add(company);
        Collections.sort(m_companies);
        m_companyMap.put(company.getName(), company);

        notifyListeners(CompanyJson.class, company, ActionTypeEnum.UPDATE);

    }




    private static void loadPrograms(boolean notify) {

        if (notify) {
            m_notifyToBeDoneSet.add(ProgramJson.class);
        }

        final ArrayList<ProgramJson > programs = new ArrayList<>();
        final String[] version = { "" };
        final String[] versionClass = { "" };
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return false;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                LoggerFactory.getLogger("Epims.Client").debug("Load Programs ... DONE ("+finished+"). Result "+success);
                if (success) {
                    m_programs = programs;

                    for (ProgramJson p : m_programs) {
                        m_programMap.put(p.getId(), p);
                    }

                    m_databaseVersion.setVersion(versionClass[0], Integer.parseInt(version[0]));

                    checkAllDataLoaded();
                }
            }
        };

        ProgramsTask task = new ProgramsTask(callback, programs, version, versionClass);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }

    private static void loadFTPSettings() {
        final FtpConfigurationJson[] ftpConfiguration = new FtpConfigurationJson[1];

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return false;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                LoggerFactory.getLogger("Epims.Client").debug("Load data for FTP ... DONE ("+finished+"). Result "+success);
                if (success) {
                    m_ftpConfiguration = ftpConfiguration[0];

                    checkAllDataLoaded();
                }
            }
        };

        FTPSettingsTask task = new FTPSettingsTask(callback, ftpConfiguration);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }

    private static void loadProjects(boolean notify) {

        if (notify) {
            m_notifyToBeDoneSet.add(ProjectJson.class);
        }

        final ArrayList<ProjectJson> projects = new ArrayList<>();
        final String[] version = { "" };
        final String[] versionClass = { "" };
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return false;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                LoggerFactory.getLogger("Epims.Client").debug("Load Projects ... DONE ("+finished+"). Result "+success);
                if (success) {
                    m_projects = projects;

                    for (ProjectJson p : m_projects) {
                        m_projectMap.put(p.getId(), p);
                    }

                    m_databaseVersion.setVersion(versionClass[0], Integer.parseInt(version[0]));

                    checkAllDataLoaded();
                }
            }
        };

        ProjectsTask task = new ProjectsTask(callback, projects, version, versionClass);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }

    private static void loadStudies(boolean notify) {

        if (notify) {
            m_notifyToBeDoneSet.add(StudyJson.class);
        }

        final ArrayList<StudyJson> studies = new ArrayList<>();
        final String[] version = { "" };
        final String[] versionClass = { "" };
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return false;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                LoggerFactory.getLogger("Epims.Client").debug("Load Studies ... DONE ("+finished+"). Result "+success);
                if (success) {
                    m_studies = studies;

                    for (StudyJson s : m_studies) {
                        m_studyMap.put(s.getId(), s);
                    }

                    m_databaseVersion.setVersion(versionClass[0], Integer.parseInt(version[0]));

                    checkAllDataLoaded();
                }
            }
        };

        StudiesTask task = new StudiesTask(callback, studies, version, versionClass);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }


    private static void loadActors(boolean notify) {

        if (notify) {
            m_notifyToBeDoneSet.add(ActorJson.class);
        }

        final ArrayList<ActorJson> actors = new ArrayList<>();
        final String[] version = { "" };
        final String[] versionClass = { "" };
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return false;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                if (success) {
                    m_actors = actors;

                    for (ActorJson a : m_actors) {
                        m_actorMap.put(a.getLogin(), a);

                        ContactJson contact = a.getContact();
                        if (contact != null) {
                            m_contactToActorMap.put(contact.getId(), a);
                        }
                    }

                    m_databaseVersion.setVersion(versionClass[0], Integer.parseInt(version[0]));

                    checkAllDataLoaded();
                }
            }
        };

        ActorsAndContactsTask task = new ActorsAndContactsTask(callback, actors, version, versionClass);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }

    private static void loadContacts(boolean notify) {

        if (notify) {
            m_notifyToBeDoneSet.add(ContactJson.class);
        }

        final ArrayList<ContactJson> contacts = new ArrayList<>();
        final String[] version = { "" };
        final String[] versionClass = { "" };
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return false;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                if (success) {
                    m_contacts = contacts;
                    Collections.sort(m_contacts);

                    for (ContactJson c : m_contacts) {
                        m_contactMap.put(c.getId(), c);
                    }

                    m_databaseVersion.setVersion(versionClass[0], Integer.parseInt(version[0]));

                    checkAllDataLoaded();
                }
            }
        };

        ContactsTask task = new ContactsTask(callback, contacts, version, versionClass);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }


    private static void loadCompanies(boolean notify) {

        if (notify) {
            m_notifyToBeDoneSet.add(CompanyJson.class);
        }

        final ArrayList<CompanyJson> companies = new ArrayList<>();
        final String[] version = { "" };
        final String[] versionClass = { "" };
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return false;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                if (success) {
                    m_companies = companies;
                    Collections.sort(m_companies);

                    for (CompanyJson c : m_companies) {
                        m_companyMap.put(c.getName(), c);
                    }

                    m_databaseVersion.setVersion(versionClass[0], Integer.parseInt(version[0]));

                    checkAllDataLoaded();
                }
            }
        };

        CompaniesTask task = new CompaniesTask(callback, companies, version, versionClass);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }

    private static void loadSampleSpecies(boolean notify) {

        if (notify) {
            m_notifyToBeDoneSet.add(SampleSpeciesJson.class);
        }

        final ArrayList<SampleSpeciesJson> sampleSpeciesJsonArrayList = new ArrayList<>();
        final String[] version = { "" };
        final String[] versionClass = { "" };
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return false;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                if (success) {
                    m_sampleSpecies = sampleSpeciesJsonArrayList;

                    for (SampleSpeciesJson s : m_sampleSpecies) {
                        m_sampleSpeciesMap.put(s.getId(), s);
                    }

                    m_databaseVersion.setVersion(versionClass[0], Integer.parseInt(version[0]));

                    checkAllDataLoaded();
                }
            }
        };

        SampleSpeciesTask task = new SampleSpeciesTask(callback, sampleSpeciesJsonArrayList, version, versionClass);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }

    private static void loadSampleTypes(boolean notify) {

        if (notify) {
            m_notifyToBeDoneSet.add(SampleTypeJson.class);
        }

        final ArrayList<SampleTypeJson> sampleTypesJsonArrayList = new ArrayList<>();
        final String[] version = { "" };
        final String[] versionClass = { "" };
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return false;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                if (success) {
                    m_sampleTypes = sampleTypesJsonArrayList;

                    for (SampleTypeJson s : m_sampleTypes) {
                        m_sampleTypesMap.put(s.getId(), s);
                    }

                    m_databaseVersion.setVersion(versionClass[0], Integer.parseInt(version[0]));

                    checkAllDataLoaded();
                }
            }
        };

        SampleTypesTask task = new SampleTypesTask(callback, sampleTypesJsonArrayList, version, versionClass);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }


    private static void loadAnalysisPriceList(boolean notify) {

        if (notify) {
            m_notifyToBeDoneSet.add(AnalysisPriceListJson.class);
        }

        ArrayList<AnalysisPriceListJson> analysisPriceList = new ArrayList<>();

        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return false;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                LoggerFactory.getLogger("Epims.Client").debug("Load Analysis Prices ... DONE ("+finished+"). Result "+success);
                if (success) {
                    m_analysisPriceListArray = analysisPriceList;

                    for (AnalysisPriceListJson priceList : m_analysisPriceListArray) {
                        m_analysisPriceListMap.put(priceList.getId(), priceList);
                    }

                    if (m_analysisPriceListArray.isEmpty()) {
                        AnalysisPriceListJson analysisPriceList = new AnalysisPriceListJson();
                        analysisPriceList.setId(-1);
                        analysisPriceList.setPriceMap(new HashMap<>());
                        m_analysisPriceListArray.add(analysisPriceList);
                    }

                    checkAllDataLoaded();
                }
            }
        };

        LoadPriceListTask task = new LoadPriceListTask(callback, analysisPriceList);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }


    private static void loadInstruments(boolean notify) {

        if (notify) {
            m_notifyToBeDoneSet.add(InstrumentJson.class);
        }

        final ArrayList<InstrumentJson> instruments = new ArrayList<>();
        final String[] version = { "" };
        final String[] versionClass = { "" };
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return false;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                if (success) {
                    m_spectrometers = new ArrayList<>();

                    for (InstrumentJson i : instruments) {
                        m_instrumentsKeyMap.put(i.getId(), i);
                        m_instrumentsNameMap.put(i.getName(), i);

                        if (i.getIsSpectrometer()) {
                            m_spectrometers.add(i);
                        }
                    }

                    Collections.sort(m_spectrometers);

                    m_databaseVersion.setVersion(versionClass[0], Integer.parseInt(version[0]));

                    checkAllDataLoaded();
                }
            }
        };

        InstrumentsTask task = new InstrumentsTask(callback, instruments, version, versionClass);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }

    private static void loadAcquisitionTypes() {
        final ArrayList<String> acquisitionTypes = new ArrayList<>();
        AbstractDatabaseCallback callback = new AbstractDatabaseCallback() {
            @Override
            public boolean mustBeCalledInAWT() {
                return false;
            }

            @Override
            public void run(boolean success, long taskId, boolean finished) {
                if (success) {
                    m_acquisitionTypes = acquisitionTypes;

                    checkAllDataLoaded();
                }
            }
        };

        AcquisitionTypesTask task = new AcquisitionTypesTask(callback, acquisitionTypes);
        AccessDatabaseThread.getAccessDatabaseThread().addTask(task);
    }



    private static void checkAllDataLoaded() {
        if ((m_ftpConfiguration == null) || (m_programs == null) || (m_projects == null) || (m_studies == null) || (m_actors == null) || (m_contacts == null) || (m_companies == null) || (m_spectrometers == null) || (m_acquisitionTypes == null) || (m_sampleSpecies == null) || (m_sampleTypes == null) || (m_analysisPriceListArray == null)) {
            return;
        }

        // post treatment on loaded data
        LoggerFactory.getLogger("Epims.Client").debug("All Data Loaded ! ");
        ProjectJson orphanProject = m_projectMap.get(-1);
        if (orphanProject == null) {
            orphanProject = new ProjectJson(-1,null, -1, "Orphan Studies", "", "", "", null, null, null, null, Boolean.FALSE);
            m_projectMap.put(orphanProject.getId(), orphanProject);
        }

        List<Integer> projectsKeys = new ArrayList<>();
        List<ProjectJson> projects = new ArrayList<>();
        projectsKeys.add(orphanProject.getId());
        projects.add(orphanProject);


        ArrayList<Integer> studiesKeys = new ArrayList<>();
        ArrayList<StudyJson> studies = new ArrayList<>();

        for (StudyJson study : m_studies) {
            if (study.getProjectId() == -1) {
                studiesKeys.add(study.getId());
                studies.add(study);
            }
        }
        orphanProject.setStudiesKeys(studiesKeys);
        orphanProject.setStudies(studies);


        ProgramJson orphanProgram = m_programMap.get(-1);
        if (orphanProgram == null) {
            orphanProgram = new ProgramJson(-1, "Orphan Projects", "", "", "", null, null, null, null, Boolean.FALSE);
            DataManager.addProgram(orphanProgram);
        }

        for (ProjectJson project : m_projects) {
            if (project.getProgramId() == -1) {
                projectsKeys.add(project.getId());
                projects.add(project);
            }
        }
        orphanProgram.setProjectsKeys(projectsKeys);
        orphanProgram.setProjects(projects);


        m_dataLoaded = true;

        synchronized (MUTEX) {
            for (DataAvailableCallback callback : m_callbacks) {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        callback.dataAvailable();
                    }
                });

            }
            m_callbacks.clear();
            for (DataAvailableCallback callback : m_everLastingcallbacks) {
                m_callbacks.add(callback);
            }
        }

        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                HashMap<DataManagerListener, HashSet<Class>> map = new HashMap();
                for (Class c : m_notifyToBeDoneSet) {
                    HashSet<DataManagerListener> listeners = m_listeners.get(c);
                    if (listeners != null) {
                        for (DataManagerListener listener : listeners) {
                            HashSet<Class> classSet = map.get(listener);
                            if (classSet == null) {
                                classSet = new HashSet<>();
                                map.put(listener, classSet);
                            }
                            classSet.add(c);
                        }
                    }
                }
                for (DataManagerListener listener : map.keySet()) {
                    HashSet<Class> classSet = map.get(listener);
                    listener.updateAll(classSet);
                }
                m_notifyToBeDoneSet.clear();
            }
        });



    }

    public interface DataManagerListener {
        public void update(Class c, Object o, ActionTypeEnum actionType);
        public void updateAll(HashSet<Class> c);
    }

}
