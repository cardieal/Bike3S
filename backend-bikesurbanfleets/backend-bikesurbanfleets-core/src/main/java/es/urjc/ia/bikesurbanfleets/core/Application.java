package es.urjc.ia.bikesurbanfleets.core;

import es.urjc.ia.bikesurbanfleets.common.util.JsonValidation;
import es.urjc.ia.bikesurbanfleets.common.util.JsonValidation.ValidationParams;
import es.urjc.ia.bikesurbanfleets.core.config.ConfigJsonReader;
import es.urjc.ia.bikesurbanfleets.common.config.GlobalInfo;
import es.urjc.ia.bikesurbanfleets.core.config.StationsInfo;
import es.urjc.ia.bikesurbanfleets.core.config.UsersInfo;
import es.urjc.ia.bikesurbanfleets.core.core.SimulationEngine;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import es.urjc.ia.bikesurbanfleets.core.exceptions.ValidationException;
import es.urjc.ia.bikesurbanfleets.systemmanager.SystemManager;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

/**
 * Hello world!
 *
 */

public class Application {
    
    private static CommandLine commandParser(String[] args) throws ParseException {
        
        Options options = new Options();
        options.addOption("globalSchema", true, "Directory to global schema validation");
        options.addOption("usersSchema", true, "Directory to users schema validation");
        options.addOption("stationsSchema", true, "Directory to stations schema validation");
        options.addOption("globalConfig", true, "Directory to the global configuration file");
        options.addOption("usersConfig", true, "Directory to the users configuration file");
        options.addOption("stationsConfig", true, "Directory to the stations configuration file");
        options.addOption("historyOutput", true, "History Path for the simulation");
        options.addOption("validator", true, "Directory to the js validator");
    
        CommandLineParser parser = new DefaultParser();
        return parser.parse(options, args);
        
    }
    
    public static void main(String[] args) throws ParseException, ValidationException {
        
        CommandLine cmd;
        try {
            cmd = commandParser(args);
        } catch (ParseException e1) {
            System.out.println("Error reading params");
            throw e1;
        }

        Map<String, String> params = new HashMap<>();
        params.put("globalSchema",  cmd.getOptionValue("globalSchema");
        params.put("usersSchema", cmd.getOptionValue("usersSchema"));
        params.put("stationsSchema", cmd.getOptionValue("stationsSchema"));
        params.put("globalConfig", cmd.getOptionValue("globalConfig"));
        params.put("usersConfig", cmd.getOptionValue("usersConfig"));
        params.put("stationsConfig", cmd.getOptionValue("stationsConfig"));
        params.put("historyOutputPath", cmd.getOptionValue("historyOutputPath"));
        params.put("validator", cmd.getOptionValue("validator"));
        
        checkParams(params);
        ConfigJsonReader jsonReader = new ConfigJsonReader(params.get("globalConfig"),
                params.get("stationsConfig"), params.get("usersConfig"));

        try {
            GlobalInfo globalInfo = jsonReader.readGlobalConfiguration();
            UsersInfo usersInfo = jsonReader.readUsersConfiguration();
            StationsInfo stationsInfo = jsonReader.readStationsConfiguration();
            System.out.println("DEBUG MODE: " + globalInfo.isDebugMode());
            if(params.get("historyOutputPath") != null) {
                globalInfo.setHistoryOutputPath(params.get("historyOutputPath"));
            }
            SystemManager systemManager = jsonReader.createSystemManager(stationsInfo, globalInfo);
            SimulationEngine simulation = new SimulationEngine(globalInfo, stationsInfo, usersInfo, systemManager);
            simulation.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private static boolean checkParams(Map<String, String> params) throws ValidationException {

        String exMessage = null; // Message for exceptions
        if(hasAllSchemasAndConfig(params)) {
            try {

                ValidationParams vParams = new ValidationParams();
                vParams.setSchemaDir(params.get("globalSchema"))
                        .setJsonDir(params.get("globalConfig"))
                        .setJsValidatorDir(params.get("validator"));

                String globalConfigValidation = validate(vParams);

                vParams.setSchemaDir(params.get("usersSchema")).setJsonDir(params.get("usersConfig"));
                String usersConfigValidation = validate(vParams);

                vParams.setSchemaDir(params.get("stationsSchema")).setJsonDir(params.get("stationsConfig"));
                String stationsConfigValidation = validate(vParams);

                if((!globalConfigValidation.equals("OK")
                        || !usersConfigValidation.equals("OK") || !stationsConfigValidation.equals("OK"))
                        && (!globalConfigValidation.equals("NODE_NOT_INSTALLED"))) {

                    exMessage = "JSON has errors \n Global configuration errors \n" + globalConfigValidation + "\n" +
                            "Stations configuration errors \n" + stationsConfigValidation + "\n" +
                            "Users configuration errors \n" + usersConfigValidation;

                } else if (globalConfigValidation.equals("NODE_NOT_INSALLED")) {

                    exMessage += "Node is necessary to execute validator: " + params.get("validator") + ". \n" +
                            "Verify if node is installed or install node";

                } else if(globalConfigValidation.equals("OK") && stationsConfigValidation.equals("OK")
                        && usersConfigValidation.equals("OK")) {

                    exMessage += "Validation configuration input: OK\n";
                }
            } catch (Exception e) {

                exMessage = "Fail executing validation + \n" + e.toString();
            }
        }
        else if(globalConfig == null || stationsConfig == null || usersConfig == null) {
            exMessage = "You should specify a configuration file";
        }
        else if((globalSchema == null || usersSchema == null || stationsSchema == null) && validator != null) {
            exMessage = "You should specify all schema paths";

        }
        else if(validator == null) {
            exMessage = "Warning, you don't specify a validator, configuration file will not be validated on backend";
        }

        if(exMessage != null) {
            throw new ValidationException(exMessage);
        }
    }

    public static boolean hasAllSchemasAndConfig(Map<String, String> params) {
        return params.get("globalSchema") != null && params.get("usersSchema") != null
                && params.get("stationsSchema") != null && params.get("globalConfig") != null
                && params.get("stationsConfig") != null && params.get("validator") != null;
    }

    public static String validate(ValidationParams vParams) throws Exception {
        return JsonValidation.validate(vParams);
    }

}
