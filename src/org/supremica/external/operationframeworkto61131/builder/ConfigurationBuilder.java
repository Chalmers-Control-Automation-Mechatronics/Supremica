package org.supremica.external.operationframeworkto61131.builder;

import org.plcopen.xml.tc6.*;
import java.util.List;

/**
 * ConfigurationBuilder.java has methods that handles the Configuration,
 * PouInstance and Global variables
 * 
 * Created: Mar 31, 2009 5:50:38 PM
 * 
 * @author LC
 * @version 1.0
 */
public class ConfigurationBuilder {

	private org.plcopen.xml.tc6.Project project;

	private org.plcopen.xml.tc6.ObjectFactory objectFactory = new org.plcopen.xml.tc6.ObjectFactory();

	public ConfigurationBuilder(org.plcopen.xml.tc6.Project project) {

		this.project = project;
		Project.Instances instances = project.getInstances();

		if (instances == null) {

			instances = objectFactory.createProjectInstances();
			project.setInstances(instances);
		}

		Project.Instances.Configurations configurations = instances
				.getConfigurations();

		if (configurations == null) {

			configurations = objectFactory
					.createProjectInstancesConfigurations();

			instances.setConfigurations(configurations);
			project.setInstances(instances);
		}

	}

	public void addPouNamesToTask(String configurationName,
			String resourceName, List<String> pouNameList) {

		Project.Instances.Configurations.Configuration configuration = getConfiguration(configurationName);
		Project.Instances.Configurations.Configuration.Resource resource = getResourceFromConfiguratoin(
				resourceName, configuration);

		// FIXME set more attributes
		// task.setInterval(value)
		// task.setPriority(value)
		// task.setSingle(value)

		for (String pouName : pouNameList) {
			String taskName = "Task_" + pouName;
			Project.Instances.Configurations.Configuration.Resource.Task task = getTaskFromResource(
					taskName, resource);
			// pouNam
			String pouInstanceName = "InstanceOf_" + pouName;
			PouInstance pouInstance = getPouInstanceFromTask(pouInstanceName,
					task);

			pouInstance.setType(pouName);

			task.getPouInstance().add(pouInstance);
			resource.getTask().add(task);

		}

		addResourceAndConfigurationToConfiguration(resource, configuration);

		return;

	}

	public void declareGlobalVariableInResource(String configurationName,
			String resourceName, org.supremica.external.operationframeworkto61131.data.VarList varList) {

		Project.Instances.Configurations.Configuration configuration = getConfiguration(configurationName);
		Project.Instances.Configurations.Configuration.Resource resource = getResourceFromConfiguratoin(
				resourceName, configuration);

		VarList interfaceVarList = objectFactory.createVarList();
		VarList constantVarList = objectFactory.createVarList();

		VarList retainVarList = objectFactory.createVarList();

		VarList constantAndRetainVarList = objectFactory.createVarList();

		for (org.supremica.external.operationframeworkto61131.data.Var var : varList.getVars()) {

			VarListPlain.Variable variable = org.supremica.external.operationframeworkto61131.builder.PouInterfaceBuilder
					.getPlainVariableFromVar(var);
			//
			// // Set name
			// variable.setName(var.getName());
			//
			// // Set initial value
			// Value value = objectFactory.createValue();
			// Value.SimpleValue simpleValue = objectFactory
			// .createValueSimpleValue();
			// simpleValue.setValue(var.getValue());
			// value.setSimpleValue(simpleValue);
			// variable.setInitialValue(value);
			//
			// // FIXME judge the type for int or time
			// // Set type
			// DataType type = new DataType();
			// if (var.getType().equals(Boolean.class)) {
			// type.setBOOL(new DataType.String());
			// } else if (var.getType().equals(Integer.class)) {
			//
			// type.setINT(new DataType.String());
			// }
			//
			// variable.setType(type);

			// Retain and Constant has to be seperate varlists and added to
			// Global seperately
			if (var.isConstant() && var.isRetain()) {

				constantAndRetainVarList.getVariable().add(variable);
			} else if (var.isConstant()) {

				constantVarList.getVariable().add(variable);
			} else if (var.isRetain()) {

				retainVarList.getVariable().add(variable);
			} else {
				interfaceVarList.getVariable().add(variable);
			}

		}

		// FIXME what if the name of the variable already exsit in the
		// resource?update ?

		resource.getGlobalVars().add(interfaceVarList);

		if (!constantVarList.getVariable().isEmpty()) {
			constantVarList.setConstant(true);
			resource.getGlobalVars().add(constantVarList);
		}

		if (!retainVarList.getVariable().isEmpty()) {
			retainVarList.setRetain(true);
			resource.getGlobalVars().add(retainVarList);
		}

		if (!constantAndRetainVarList.getVariable().isEmpty()) {

			constantAndRetainVarList.setConstant(true);
			constantAndRetainVarList.setConstant(true);
			resource.getGlobalVars().add(constantAndRetainVarList);
		}

		addResourceAndConfigurationToConfiguration(resource, configuration);

		return;

	}

	private void addResourceAndConfigurationToConfiguration(
			Project.Instances.Configurations.Configuration.Resource resource,
			Project.Instances.Configurations.Configuration configuration) {

		configuration.getResource().add(resource);

		Project.Instances instances = project.getInstances();
		Project.Instances.Configurations configurations = instances
				.getConfigurations();
		configurations.getConfiguration().add(configuration);

		instances.setConfigurations(configurations);
		project.setInstances(instances);

		return;
	}

	public Project.Instances.Configurations.Configuration getConfiguration(
			String configurationName) {

		Project.Instances.Configurations.Configuration configuration = null;
		for (Project.Instances.Configurations.Configuration iConfiguration : project
				.getInstances().getConfigurations().getConfiguration()) {

			if (iConfiguration.getName().equals(configurationName)) {

				configuration = iConfiguration;
				project.getInstances().getConfigurations().getConfiguration()
						.remove(iConfiguration);

				return configuration;
			}
		}

		if (configuration == null) {

			configuration = objectFactory
					.createProjectInstancesConfigurationsConfiguration();
			configuration.setName(configurationName);

		}

		return configuration;

	}

	public Project.Instances.Configurations.Configuration.Resource getResourceFromConfiguratoin(
			String resourceName,
			Project.Instances.Configurations.Configuration configuration) {

		Project.Instances.Configurations.Configuration.Resource resource = null;

		for (Project.Instances.Configurations.Configuration.Resource iResource : configuration
				.getResource()) {

			if (iResource.getName().equals(resourceName)) {

				resource = iResource;
				// live list
				configuration.getResource().remove(iResource);

				break;
			}
		}

		if (resource == null) {

			resource = objectFactory
					.createProjectInstancesConfigurationsConfigurationResource();

			resource.setName(resourceName);
		}

		return resource;
	}

	public Project.Instances.Configurations.Configuration.Resource.Task getTaskFromResource(
			String taskName,
			Project.Instances.Configurations.Configuration.Resource resource) {

		Project.Instances.Configurations.Configuration.Resource.Task task = null;
		for (Project.Instances.Configurations.Configuration.Resource.Task iTask : resource
				.getTask()) {

			if (iTask.getName().equals(taskName)) {

				task = iTask;
				// live list
				resource.getTask().remove(iTask);

				break;
			}

		}

		// Task does not exsit
		if (task == null) {

			task = objectFactory
					.createProjectInstancesConfigurationsConfigurationResourceTask();
			task.setName(taskName);
		}

		return task;

	}

	public PouInstance getPouInstanceFromTask(String pouInstanceName,
			Project.Instances.Configurations.Configuration.Resource.Task task) {

		PouInstance pouInstance = null;
		for (PouInstance iPouInstance : task.getPouInstance()) {

			if (iPouInstance.getName().equals(pouInstanceName)) {

				pouInstance = iPouInstance;
				// live list
				task.getPouInstance().remove(iPouInstance);
				break;
			}

		}

		// Task does not exsit
		if (pouInstance == null) {

			pouInstance = objectFactory.createPouInstance();
			pouInstance.setName(pouInstanceName);
		}

		return pouInstance;

	}

}
