package platform.types.incisive.actions;

import config.actions.ActionUpdateToRunning;
import exceptions.InternalException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static utils.HttpMethods.patchMultipartMethod;

public class ActionUpdateToRunningImplementation {

    public static void run(ActionUpdateToRunning action) throws InternalException {
        Set<Integer> expectedStatusCode = new HashSet<>();
        expectedStatusCode.add(200);
        patchMultipartMethod(
                action.getUpdateStatusUrl(),
                new JSONObject("{}"),
                new ArrayList<>(),
                new ArrayList<>(),
                expectedStatusCode,
                "Error while updating status to running"
        );
    }

}
