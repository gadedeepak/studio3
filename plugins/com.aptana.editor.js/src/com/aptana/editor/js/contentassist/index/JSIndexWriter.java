package com.aptana.editor.js.contentassist.index;

import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.aptana.core.util.StringUtil;
import com.aptana.editor.js.JSTypes;
import com.aptana.editor.js.contentassist.model.FunctionElement;
import com.aptana.editor.js.contentassist.model.ParameterElement;
import com.aptana.editor.js.contentassist.model.PropertyElement;
import com.aptana.editor.js.contentassist.model.ReturnTypeElement;
import com.aptana.editor.js.contentassist.model.TypeElement;
import com.aptana.editor.js.contentassist.model.UserAgentElement;
import com.aptana.index.core.Index;

public class JSIndexWriter
{
	private static final URI METADATA_LOCATION = URI.create(""); //$NON-NLS-1$
	private static Map<UserAgentElement,String> keysByUserAgent = new HashMap<UserAgentElement,String>();
	static Map<String,UserAgentElement> userAgentsByKey = new HashMap<String,UserAgentElement>();
	
	private JSMetadataReader _reader;
	private int _descriptionCount;
	private int _parameterCount;
	private int _returnTypeCount;

	/**
	 * JSMetadataIndexer
	 */
	public JSIndexWriter()
	{
		this._reader = new JSMetadataReader();
	}

	/**
	 * getDocumentPath
	 * 
	 * @return
	 */
	protected URI getDocumentPath()
	{
		return URI.create(JSIndexConstants.METADATA);
	}
	
	/**
	 * loadXML
	 * 
	 * @param stream
	 * @throws ScriptDocException
	 */
	public void loadXML(InputStream stream) throws ScriptDocException
	{
		this._reader.loadXML(stream);
	}

	/**
	 * writeDescription
	 * 
	 * @param description
	 */
	protected String writeDescription(Index index, String description, URI location)
	{
		String indexString;
		
		if (description != null && description.length() > 0)
		{
			indexString = Integer.toString(this._descriptionCount++);
			
			String value = indexString + JSIndexConstants.DELIMITER + description;
			
			index.addEntry(JSIndexConstants.DESCRIPTION, value, location);
		}
		else
		{
			indexString = JSIndexConstants.NO_ENTRY;
		}
		
		return indexString;
	}
	
	/**
	 * writeFunction
	 * 
	 * @param index
	 * @param function
	 * @param location
	 */
	protected void writeFunction(Index index, FunctionElement function, URI location)
	{
		String parametersKey = this.writeParameters(index, function.getParameters(), location);
		String returnTypesKey = this.writeReturnTypes(index, function.getReturnTypes(), location);
		String descriptionKey = this.writeDescription(index, function.getDescription(), location);
		// SinceElement[] sinceList = function.getSinceList();
		
		String value = StringUtil.join(
			JSIndexConstants.DELIMITER,
			function.getName(),
			function.getOwningType().getName(),
			descriptionKey,
			parametersKey,
			returnTypesKey,
			StringUtil.join(JSIndexConstants.SUB_DELIMITER, this.writeUserAgents(index, function.getUserAgents()))
		);

		index.addEntry(JSIndexConstants.FUNCTION, value, location);
	}

	/**
	 * writeParameters
	 * 
	 * @param index
	 * @param parameters
	 * @return
	 */
	protected String writeParameters(Index index, ParameterElement[] parameters, URI location)
	{
		List<String> keyList = new ArrayList<String>();
		String indexString = Integer.toString(this._parameterCount++);
		
		keyList.add(indexString);
		
		for (int i = 0; i < parameters.length; i++)
		{
			ParameterElement parameter = parameters[i];
			String name = parameter.getName();
			String usage = parameter.getUsage();
			String types = StringUtil.join(",", parameter.getTypes()); //$NON-NLS-1$
			
			keyList.add(name + "," + usage + "," + types); //$NON-NLS-1$ //$NON-NLS-2$
		}
		
		String value = StringUtil.join(JSIndexConstants.DELIMITER, keyList);
		
		index.addEntry(JSIndexConstants.PARAMETERS, value, location);
		
		return indexString;
	}
	
	/**
	 * writeProperty
	 * 
	 * @param index
	 * @param property
	 * @param location
	 */
	protected void writeProperty(Index index, PropertyElement property, URI location)
	{
		String propertyTypesKey = this.writeReturnTypes(index, property.getTypes(), location);
		String descriptionKey = this.writeDescription(index, property.getDescription(), location);
		// SinceElement[] sinceList = property.getSinceList();
		
		String value = StringUtil.join(
			JSIndexConstants.DELIMITER,
			property.getName(),
			property.getOwningType().getName(),
			descriptionKey,
			propertyTypesKey,
			StringUtil.join(JSIndexConstants.SUB_DELIMITER, this.writeUserAgents(index, property.getUserAgents()))
		);

		index.addEntry(JSIndexConstants.PROPERTY, value, location);
	}

	/**
	 * writeReturnTypes
	 * 
	 * @param index
	 * @param returnTypes
	 * @return
	 */
	protected String writeReturnTypes(Index index, ReturnTypeElement[] returnTypes, URI location)
	{
		List<String> keyList = new ArrayList<String>();
		String indexString = Integer.toString(this._returnTypeCount++);
		
		keyList.add(indexString);
		
		for (ReturnTypeElement returnType : returnTypes)
		{
			String type = returnType.getType();
			String descriptionKey = this.writeDescription(index, returnType.getDescription(), location);
			
			keyList.add(type + "," + descriptionKey); //$NON-NLS-1$
		}
		
		String value = StringUtil.join(JSIndexConstants.DELIMITER, keyList);
		
		index.addEntry(JSIndexConstants.RETURN_TYPES, value, location);
		
		return indexString;
	}
	
	/**
	 * writeToIndex
	 * 
	 * @param index
	 */
	public void writeToIndex(Index index)
	{
		TypeElement[] types = this._reader.getTypes();

		for (TypeElement type : types)
		{
			this.writeType(index, type);
		}
	}

	/**
	 * writeType
	 * 
	 * @param index
	 * @param type
	 */
	public void writeType(Index index, TypeElement type)
	{
		this.writeType(index, type, METADATA_LOCATION);
	}
	
	/**
	 * writeType
	 * 
	 * @param index
	 * @param type
	 * @param location
	 */
	public void writeType(Index index, TypeElement type, URI location)
	{
		// write type entry
		List<String> parentTypes = type.getParentTypes();
		String descriptionKey = this.writeDescription(index, type.getDescription(), location);
		// SinceElement[] sinceList = type.getSinceList();
		// UserAgentElement[] userAgents = type.getUserAgents();

		// calculate key value and add to index
		String value = StringUtil.join(
			JSIndexConstants.DELIMITER,
			type.getName(),
			(parentTypes != null && parentTypes.isEmpty() == false)
				? StringUtil.join(",", parentTypes) //$NON-NLS-1$
				: (type.equals(JSTypes.OBJECT) == false) //$NON-NLS-1$
					? JSTypes.OBJECT //$NON-NLS-1$
					: "", //$NON-NLS-1$
			descriptionKey
		);

		index.addEntry(JSIndexConstants.TYPE, value, location);

		// write properties
		for (PropertyElement property : type.getProperties())
		{
			if (property instanceof FunctionElement)
			{
				this.writeFunction(index, (FunctionElement) property, location);
			}
			else
			{
				this.writeProperty(index, property, location);
			}
		}
	}
	
	/**
	 * writeUserAgent
	 * 
	 * @param index
	 * @param userAgent
	 * @return
	 */
	protected String writeUserAgent(Index index, UserAgentElement userAgent)
	{
		String key = keysByUserAgent.get(userAgent);
		
		if (key == null)
		{
			key = Integer.toString(keysByUserAgent.size());
			
			String[] columns = new String[] {
				key,
				userAgent.getDescription(),
				userAgent.getOS(),
				userAgent.getPlatform(),
				userAgent.getVersion()
			};
			String value = StringUtil.join(JSIndexConstants.DELIMITER, columns);
			
			index.addEntry(JSIndexConstants.USER_AGENT, value, this.getDocumentPath());
			
			keysByUserAgent.put(userAgent, key);
			userAgentsByKey.put(key, userAgent);
		}
		
		return key;
	}
	
	/**
	 * writeUserAgents
	 * 
	 * @param userAgents
	 * @return
	 */
	protected List<String> writeUserAgents(Index index, List<UserAgentElement> userAgents)
	{
		List<String> keys = new ArrayList<String>();
		
		for (UserAgentElement userAgent : userAgents)
		{
			keys.add(this.writeUserAgent(index, userAgent));
		}
		
		return keys;
	}
}
