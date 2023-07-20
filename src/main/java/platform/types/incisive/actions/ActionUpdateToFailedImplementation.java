package platform.types.incisive.actions;

import config.actions.ActionUpdateToFailed;
import exceptions.InternalException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static utils.HttpMethods.patchMultipartMethod;

public class ActionUpdateToFailedImplementation {

    public static void run(ActionUpdateToFailed action) throws InternalException {
        JSONObject entity = new JSONObject();
        entity.put("message", action.getMessage());
        Set<Integer> expectedStatusCode = new HashSet<>();
        expectedStatusCode.add(200);
        patchMultipartMethod(
                action.getUpdateStatusUrl(),
                entity,
                new ArrayList<>(),
                new ArrayList<>(),
                expectedStatusCode,
                "Error while updating status to failed"
        );
    }

}
