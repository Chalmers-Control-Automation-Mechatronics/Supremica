package org.supremica.util.BDD;

import java.io.*;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import javax.xml.parsers.*;

class XMLReader
	extends DefaultHandler
{
	private final String TYPE_AUTOMATA = "Automata",
						 TYPE_AUTOMATON = "Automaton", TYPE_EVENT = "Event",
						 TYPE_STATE = "State", TYPE_ARC = "Transition",
						 VALUE_NAME = "name", VALUE_ID = "id",
						 VALUE_LABEL = "label", VALUE_TYPE = "type",
						 VALUE_INITIAL = "initial",
						 VALUE_MARKED = "accepting",
						 VALUE_FORBIDDEN = "forbidden",
						 VALUE_CONTROLLABLE = "controllable",
						 VALUE_PRIORITIZED = "prioritized",
						 VALUE_EVENT = "event", VALUE_FROM = "source", VALUE_TO = "dest"
	;
	private Automata automata;

	XMLReader(Automata automata, String filename)
	    throws IOException, ParserConfigurationException, SAXException
	{
		this.automata = automata;

		SAXParserFactory parserFactory = SAXParserFactory.newInstance();
		SAXParser parser = parserFactory.newSAXParser();

		parser.parse(new File(filename), this);
	}

	private int getType(String name)
	{
		if (name == null)
		{
			return Automaton.TYPE_UNKNOWN;
		}

		if (name.equals("Plant"))
		{
			return Automaton.TYPE_PLANT;
		}

		if (name.equals("Specification"))
		{
			return Automaton.TYPE_SPEC;
		}

		if (name.equals("Supervisor"))
		{
			return Automaton.TYPE_SUPERVISOR;
		}

		return Automaton.TYPE_UNKNOWN;
	}

	private boolean getBooleanValue(Attributes attr, String query, boolean def)
	{
		String txt = attr.getValue(query);

		if (txt == null)
		{
			return def;
		}

		if (txt.equals("false"))
		{
			return false;
		}

		if (txt.equals("true"))
		{
			return true;
		}

		return def;
	}

	public void startElement(String uri, String localName, String qName, Attributes attributes)
	    throws SAXException
	{
	    try {
		if (qName.equals(TYPE_AUTOMATON))
		{
			String name = attributes.getValue(VALUE_NAME);
			int type = getType(attributes.getValue(VALUE_TYPE));

			automata.createAutomaton(name);
			automata.getCurrent().setType(type);
		}
		else if (qName.equals(TYPE_STATE))
		{
			Automaton c = automata.getCurrent();
			String name = attributes.getValue(VALUE_NAME);
			String name_id = attributes.getValue(VALUE_ID);

			if (name == null)
			{
				name = name_id;
			}
			else if (name_id == null)
			{
				name_id = name;    // just for fun :)
			}

			boolean initial = getBooleanValue(attributes, VALUE_INITIAL, false);
			boolean marked = getBooleanValue(attributes, VALUE_MARKED, false);
			boolean forbidden = getBooleanValue(attributes, VALUE_FORBIDDEN, false);

			c.addState(name, name_id, initial, marked, forbidden);
		}
		else if (qName.equals(TYPE_EVENT))
		{
			Automaton c = automata.getCurrent();
			String id = attributes.getValue(VALUE_ID);
			String label = attributes.getValue(VALUE_LABEL);

			if (label == null)
			{
				label = id;
			}

			boolean co = getBooleanValue(attributes, VALUE_CONTROLLABLE, true);
			boolean p = getBooleanValue(attributes, VALUE_PRIORITIZED, true);

			c.addEvent(label, id, co, p);
		}
		else if (qName.equals(TYPE_ARC))
		{
			Automaton c = automata.getCurrent();
			String name = attributes.getValue(VALUE_EVENT);
			String from = attributes.getValue(VALUE_FROM);
			String to = attributes.getValue(VALUE_TO);

			c.addArc(name, from, to);
		}
	    } catch(BDDException e) {
		throw new SAXException(e.toString());
	    }
	}

	public void endElement(String uri, String localName, String qName)
	    throws SAXException

	{
	    try {
		if (qName.equals(TYPE_AUTOMATON))
		{
			automata.getCurrent().close();

			// Options.out.println("read automaton " + automata.getCurrent().getName() );
		}
		else if (qName.equals(TYPE_AUTOMATA))
		{
			automata.close();
		}
	    } catch(BDDException e) {
		throw new SAXException(e.toString());
	    }
	}
}
