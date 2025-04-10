package fr.epims.ui.tree;

import fr.edyp.epims.json.ProgramJson;
import fr.edyp.epims.json.ProjectJson;
import fr.edyp.epims.json.StudyJson;

public interface TreeSelectionInterface {

    public void setProgram(ProgramJson p);

    public void setProject(ProjectJson p);

    public void setStudy(StudyJson s, boolean doubleClick);
}
