package net.isger.brick.struts;

import java.io.Serializable;
import java.util.Map;

import net.isger.brick.core.BaseCommand;
import net.isger.brick.core.Command;
import net.isger.brick.core.Console;
import net.isger.brick.core.ConsoleManager;
import net.isger.brick.core.Context;
import net.isger.brick.util.Reflects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.inject.Container;

public class BrickEnvoy implements Serializable {

    private static final long serialVersionUID = -2742773505722456414L;

    public static final String DEF_BEAN_NAME = "brick";

    public static final String COMMAND = "brick.command";

    private static final Logger LOG;

    static {
        LOG = LoggerFactory.getLogger(BrickEnvoy.class);
    }

    public static Command getCommand(ActionContext context) {
        Command cmd = (Command) context.get(COMMAND);
        if (cmd == null) {
            cmd = new BaseCommand();
            cmd.setParameters(context.getParameters());
            context.put(COMMAND, cmd);
        }
        return cmd;
    }

    public static Command getCommand() {
        return getCommand(ActionContext.getContext());
    }

    public static <T> T getModel(Class<T> clazz) {
        return getModel(clazz, null);
    }

    @SuppressWarnings("unchecked")
    public static <T> T getModel(Class<T> clazz, String name) {
        Map<String, Object> params = getCommand().getParameters();
        if (name != null) {
            params = (Map<String, Object>) Reflects.canonicalize(params).get(
                    name);
        }
        try {
            return Reflects.newInstance(clazz, params);
        } catch (Exception e) {
            LOG.warn("Failure access request parameters", e);
        }
        return null;
    }

    public static Context getContext() {
        Context context = Context.getActionContext();
        if (context == null) {
            Container container = ActionContext.getContext().getContainer();
            context = container.getInstance(Context.class, DEF_BEAN_NAME);
            Context.setActionContext(context);
        }
        return context;
    }

    public static Console getConsole() {
        Container container = ActionContext.getContext().getContainer();
        ConsoleManager manager = container.getInstance(ConsoleManager.class,
                DEF_BEAN_NAME);
        return manager.getConsole();
    }

    public static void execute(Command cmd) {
        getContext().set(Context.COMMAND, cmd);
        getConsole().execute();
    }

}
