package org.supremica.external.processeditor.xml;

import java.io.File;
import java.util.Iterator;

import org.supremica.manufacturingTables.xsd.processeditor.Activity;
import org.supremica.manufacturingTables.xsd.processeditor.Algebraic;
import org.supremica.manufacturingTables.xsd.processeditor.Attribute;
import org.supremica.manufacturingTables.xsd.processeditor.ObjectFactory;
import org.supremica.manufacturingTables.xsd.processeditor.OperationReferenceType;
import org.supremica.manufacturingTables.xsd.processeditor.ROP;
import org.supremica.manufacturingTables.xsd.processeditor.ROPType;
import org.supremica.manufacturingTables.xsd.processeditor.Relation;
import org.supremica.manufacturingTables.xsd.processeditor.RelationType;

public class XmlConverter implements FileConverter {
    private final ObjectFactory factory = new ObjectFactory();

    private ROP rop = null;
    private Relation[] relation = new Relation[0];
    private Activity activity = null;

    public XmlConverter() {}

    @Override
    public Object open(final File file, final FileConverter fc) {
	return null;
    }

    @Override
    public void save(final File file, final Object rop, final FileConverter fc) {
	//DEBUG
	//System.out.println("XmlConverter.save()");
	//END DEBUG
	if(rop instanceof ROP) {
	    try{
		fc.newResource(((ROP)rop).getMachine());
		saveRelation(((ROP)rop).getRelation(), fc);
		fc.saveResource(file);
	    }catch(final Exception ex) {}
	}
    }
    public static void saveRelation(final Object relation, final FileConverter fc) {
	//DEBUG
	//System.out.println("XmlConverter.saveRelation()");
	//END DEBUG
	if(relation instanceof Relation) {
	    fc.newRelation(((Relation)relation).getType().name());
	    final Iterator<?> iterator = ((Relation)relation).getActivityRelationGroup().iterator();
	    while(iterator.hasNext()) {
		final Object next = iterator.next();
		if(next instanceof Relation) {
		    saveRelation(next, fc);
		}else if(next instanceof Activity) {
		    saveActivity(next, fc);
		}
	    }
	    fc.addRelation();
	}
    }
    public static void saveActivity(final Object activity, final FileConverter fc) {
	//DEBUG
	//System.out.println("XmlConverter.saveActivity()");
	//END DEBUG
	if(activity instanceof Activity) {
	    String opText = "";
	    if(((Activity)activity).getOperation() != null && !((Activity)activity).getOperation().equals("")) {
	    	opText = ((Activity)activity).getOperation();
	    }else {
	    	opText = ((Activity)activity).getOperation();
	    }
	    fc.newOperation(opText);
	    if(((Activity)activity).getProperties() != null) {
		final Iterator<Attribute> iterator = ((Activity)activity).getProperties().getAttribute().iterator();
		while(iterator.hasNext()) {
		    final Object next = iterator.next();
		    if(next instanceof Attribute) {
			fc.addAttribute(((Attribute)next).getAttributeValue()+"@"+((Attribute)next).getType());
		    }
		}
	    }
	    fc.addOperation();
	}
    }
@Override
public void newResource(final String machine,final String id) {
	//DEBUG
	System.out.println("newResource("+machine+")");
	//END DEBUG
	try {
	    rop = factory.createROP();
	    rop.setType(ROPType.COP);
	    rop.setId(id);
	    rop.setMachine(machine);
	}catch(final Exception ex) {
	    //DEBUG
	    System.out.println("ERROR newResource");
	    //END DEBUG
	}
    }
    @Override
    public void newResource(final String machine) {
	//DEBUG
	System.out.println("newResource("+machine+")");
	//END DEBUG
	try {
	    rop = factory.createROP();
	    rop.setType(ROPType.COP);
	    rop.setId("1");
	    rop.setMachine(machine);
	}catch(final Exception ex) {
	    //DEBUG
	    System.out.println("ERROR newResource");
	    //END DEBUG
	}
    }

    @Override
    public void newRelation(final String name)
    {
      final RelationType type = RelationType.valueOf(name);
      newRelation(type);
    }

    public void newRelation(final RelationType relationType) {
	//DEBUG
	System.out.println("newRelation("+relationType+")");
	//END DEBUG
	try {
	    final Relation[] tmpRelation = new Relation[relation.length+1];
	    for(int i = 0; i < relation.length; i++) {
	    	tmpRelation[i] = relation[i];
	    }

	    tmpRelation[relation.length] = factory.createRelation();
	    relation = tmpRelation;
	    switch (relationType) {
	    case SEQUENCE:
	      relation[relation.length-1].setType(RelationType.SEQUENCE);
	      break;
	    case ALTERNATIVE:
	      relation[relation.length-1].setType(RelationType.ALTERNATIVE);
	      relation[relation.length-1].getAlgebraic().setUnextended(true);
	      relation[relation.length-1].getAlgebraic().setCompressed(true);
	      break;
	    case PARALLEL:
	      relation[relation.length-1].setType(RelationType.PARALLEL);
	      final Algebraic algebraic = factory.createAlgebraic();
	      algebraic.setUnextended(true);
	      algebraic.setCompressed(true);
	      relation[relation.length-1].setAlgebraic(algebraic);
	      break;
	    case ARBITRARY:
	      relation[relation.length-1].setType(RelationType.ARBITRARY);
	      relation[relation.length-1].getAlgebraic().setUnextended(true);
	      relation[relation.length-1].getAlgebraic().setCompressed(true);
	      break;
	    }
	}catch(final Exception ex) {
	    //DEBUG
	    System.out.println("ERROR newRelation");
	    //END DEBUG
	}
    }
    @Override
    public void addRelation() {
	try {
	    //DEBUG
	    System.out.println("addRelation()");
	    //END DEBUG
	    if(relation.length == 1) {
		rop.setRelation(relation[0]);
	    }else if(relation.length > 1){
		relation[relation.length-2].getActivityRelationGroup().add(relation[relation.length-1]);
		final Relation[] tmpRelation = new Relation[relation.length-1];
		for(int i = 0; i < tmpRelation.length; i++) {
		    tmpRelation[i] = relation[i];
		}
		relation = tmpRelation;
	    }
	}catch(final Exception ex) {
	    //DEBUG
	    System.out.println("ERROR addRelation");
	    //END DEBUG
	}
    }
    @Override
    public void newOperation(final String operation) {
	try {
	    //DEBUG
	    System.out.println("newOperation("+operation+")");
	    //END DEBUG
	    activity = factory.createActivity();
	    activity.setOperation(operation);
	}catch(final Exception ex) {}
    }
    @Override
    public void addOperation() {
	try {
	    //DEBUG
	    System.out.println("addOperation()");
	    //END DEBUG
	    if(activity != null) {
		relation[relation.length-1].getActivityRelationGroup().add(activity);
	    }
	}catch(final Exception ex) {
	    //DEBUG
	    System.out.println("ERROR addOperation");
	    //END DEBUG
	}
    }
    @Override
    public void addAttribute(final String att) {
	try {
	    //DEBUG
	    System.out.println("addAttribute("+att+")");
	    //DEBUG
	    final Attribute attribute = factory.createAttribute();
	    final int index = att.indexOf('@');
	    if(index != -1) {
		attribute.setAttributeValue(att.substring(0,index));
		attribute.setType(att.substring(index+1,att.length()));
		attribute.setInvisible(true);
		if(activity != null) {
		    if(activity.getProperties() == null) {
			activity.setProperties(factory.createProperties());
		    }
		    activity.getProperties().getAttribute().add(attribute);
		}
	    }

	}catch(final Exception ex) {
	    //DEBUG
	    System.out.println("ERROR addAttribute");
	    //END DEBUG
	}
    }
    @Override
    public void addPredecessor(final String pred) {
	try {
	    //DEBUG
	    System.out.println("addPredecessor("+pred+")");
	    //END DEBUG
	    final OperationReferenceType predecessor = factory.createOperationReferenceType();
	    final int index = pred.indexOf('@');
	    if(index != -1) {
		predecessor.setOperation(pred.substring(0,index));
		predecessor.setMachine(pred.substring(index+1,pred.length()));
		if(activity != null) {
		    if(activity.getPrecondition() == null) {
			activity.setPrecondition(factory.createPrecondition());
		    }
		    activity.getPrecondition().getPredecessor().add(predecessor);
		}
	    }else {
		predecessor.setOperation(pred);
		predecessor.setMachine("");
		if(activity != null) {
		    activity.setPrecondition(factory.createPrecondition());
		}
		activity.getPrecondition().getPredecessor().add(predecessor);
	    }
	}catch(final Exception ex) {}
    }
    @Override
    public void addDescription(final String desc) {
	try {
	    //DEBUG
	    System.out.println("addDescription("+desc+")");
	    //DEBUG
	    if(activity != null) {
	    	activity.setDescription(desc);
	    }
	}catch(final Exception ex) {
	    //DEBUG
	    System.out.println("ERROR addAttribute");
	    //END DEBUG
	}
    }
    @Override
    public Object getResource() {
	return rop;
    }
    @Override
    public void saveResource(final File file) {
	final Loader loader = new Loader();
	loader.save(getResource(),file);
	/*
	loader.save(((FileConverter)xmlConverterListModel.get(index)).getResource(),
			    new File(file.getParent()+"//"+"test"+
			    Integer.toString(index)+".xml"));	*/
    }

    @Override
    public FileConverter newInstance() {
	return new XmlConverter();
    }
}
