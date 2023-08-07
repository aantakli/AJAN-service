package de.dfki.asr.ajan.pluginsystem.mosimplugin.extensions.instructions;

import de.dfki.asr.ajan.pluginsystem.mosimplugin.utils.MOSIMUtil;
import de.dfki.asr.ajan.pluginsystem.mosimplugin.vocabularies.MOSIMVocabulary;
import de.mosim.mmi.constraints.MConstraint;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Value;

@Data
public class InstructionParameters {
    private String mmu = "";
    private String actionName;
    private String finalEvent = "end";
    private ArrayList<Value> properties;
    private Map<String, String> instProps;

    private ArrayList<Value> constraints;
    private List<MConstraint> mConstraints = null;
    private String avatarID = "";
    private String startCond = "";
    private String endCond = "";
	
    private String cosimHost;
    private int cosimPort;

	public InstructionParameters(Model inputModel) {
		try {
            mmu = MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_MMU);
			avatarID = MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_AVATAR_ID);
			if (avatarID.isEmpty())
				avatarID = "";
			finalEvent = MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_FINAL_EVENT);
			if (finalEvent.isEmpty())
				finalEvent = "end";
            actionName = MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_ACTION_NAME);
            properties = MOSIMUtil.getObjects(inputModel, null, MOSIMVocabulary.HAS_MMU_PROPERTY);
            instProps = MOSIMUtil.createGeneralProperties(properties, inputModel);
            constraints = MOSIMUtil.getObjects(inputModel, null, MOSIMVocabulary.HAS_CONSTRAINT);
            if (!constraints.isEmpty()) {
                mConstraints = MOSIMUtil.createConstraints(MOSIMUtil.getConstraintObj64(constraints, inputModel));
            }
            startCond = MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_START_CONDITION);
            endCond = MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_END_CONDITION);
            cosimHost = MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_HOST);
            cosimPort = Integer.parseInt(MOSIMUtil.getObject(inputModel, null, MOSIMVocabulary.HAS_PORT));
        } catch (URISyntaxException | NumberFormatException ex) {

        }
	}
}
