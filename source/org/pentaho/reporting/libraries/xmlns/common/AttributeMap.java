/*
 * This program is free software; you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License, version 2.1 as published by the Free Software
 * Foundation.
 *
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, you can obtain a copy at http://www.gnu.org/licenses/old-licenses/lgpl-2.1.html
 * or from the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2006 - 2009 Object Refinery Ltd, Pentaho Corporation and Contributors.  All rights reserved.
 */

package org.pentaho.reporting.libraries.xmlns.common;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.pentaho.reporting.libraries.base.util.Empty;

/**
 * A attribute map holding &lt;namspace;name&gt;-value pairs.
 *
 * @author Thomas Morgner
 */
public class AttributeMap implements Serializable, Cloneable
{
  private static final String[] EMPTY_NAMESPACES = new String[0];

  private static final long serialVersionUID = -7442871030874215436L;
  private HashMap namespaces;
  private String singletonNamespace;
  private HashMap singletonContent;

  /**
   * Default constructor.
   */
  public AttributeMap()
  {
  }

  /**
   * Creates a new attibute map using the given parameter as source for the initial values.
   *
   * @param copy the attribute map that should be copied.
   */
  public AttributeMap(final AttributeMap copy)
  {
    if (copy == null)
    {
      return;
    }

    if (copy.singletonNamespace != null)
    {
      singletonNamespace = copy.singletonNamespace;
      singletonContent = (HashMap) copy.singletonContent.clone();
    }

    if (copy.namespaces == null)
    {
      return;
    }

    namespaces = (HashMap) copy.namespaces.clone();
    final Iterator entries = namespaces.entrySet().iterator();
    while (entries.hasNext())
    {
      final Map.Entry entry = (Map.Entry) entries.next();
      final HashMap value = (HashMap) entry.getValue();
      entry.setValue(value.clone());
    }
  }

  /**
   * Creates a copy of this map.
   *
   * @return the clone.
   * @noinspection CloneDoesntDeclareCloneNotSupportedException
   */
  public Object clone()
  {
    try
    {
      final AttributeMap map = (AttributeMap) super.clone();
      if (singletonNamespace != null)
      {
        map.singletonContent = (HashMap) singletonContent.clone();
      }
      if (namespaces != null)
      {
        map.namespaces = (HashMap) namespaces.clone();
        final Iterator entries = map.namespaces.entrySet().iterator();
        while (entries.hasNext())
        {
          final Map.Entry entry = (Map.Entry) entries.next();
          final HashMap value = (HashMap) entry.getValue();
          entry.setValue(value.clone());
        }
      }
      return map;
    }
    catch (final CloneNotSupportedException cne)
    {
      // ignored
      throw new IllegalStateException("Cannot happen: Clone not supported exception");
    }
  }

  /**
   * Defines the attribute for the given namespace and attribute name.
   *
   * @param namespace the namespace under which the value should be stored.
   * @param attribute the attribute name under which the value should be stored within the namespace.
   * @param value     the value.
   * @return the previously stored value at that position.
   */
  public Object setAttribute(final String namespace,
                             final String attribute,
                             final Object value)
  {
    if (namespace == null)
    {
      throw new NullPointerException("Attribute namespace must not be null");
    }
    if (attribute == null)
    {
      throw new NullPointerException("Attribute name must not be null");
    }


    if (singletonNamespace == null)
    {
      if (value != null)
      {
        singletonNamespace = namespace;
        singletonContent = new HashMap();
        singletonContent.put(attribute, value);
      }
      return null;
    }
    
    if (namespace.equals(singletonNamespace))
    {
      if (value == null)
      {
        return singletonContent.remove(attribute);
      }
      else
      {
        return singletonContent.put(attribute, value);
      }
    }

    if (namespaces == null)
    {
      if (value == null)
      {
        return null;
      }

      namespaces = new HashMap();
    }

    final HashMap attrs = (HashMap) namespaces.get(namespace);
    if (attrs == null)
    {
      if (value == null)
      {
        return null;
      }

      final HashMap newAtts = new HashMap();
      newAtts.put(attribute, value);
      namespaces.put(namespace, newAtts);
      return null;
    }
    else
    {
      if (value == null)
      {
        final Object retval = attrs.remove(attribute);
        if (attrs.isEmpty())
        {
          namespaces.remove(namespace);
        }
        return retval;
      }
      else
      {
        return attrs.put(attribute, value);
      }
    }
  }

  /**
   * Returns the attribute value for the given namespace and attribute-name.
   *
   * @param namespace the namespace.
   * @param attribute the attribute name.
   * @return the value or null, if there is no such namespace/attribute name combination.
   */
  public Object getAttribute(final String namespace,
                             final String attribute)
  {
    if (namespace == null)
    {
      throw new NullPointerException("Attribute namespace must not be null");
    }
    if (attribute == null)
    {
      throw new NullPointerException("Attribute name must not be null");
    }
    if (singletonNamespace == null)
    {
      return null;
    }
    if (namespace.equals(singletonNamespace))
    {
      return singletonContent.get(attribute);
    }

    if (namespaces == null)
    {
      return null;
    }

    final HashMap attrs = (HashMap) namespaces.get(namespace);
    if (attrs == null)
    {
      return null;
    }
    else
    {
      return attrs.get(attribute);
    }
  }

  /**
   * Looks up all namespaces and returns the value from the first namespace that has this attribute defined. As the
   * order of the namespaces is not defined, this returns a random value and the namespace used is undefined if more
   * than one namespace contains the same attribute.
   *
   * @param attribute the the attribute name.
   * @return the object from the first namespace that carries this attribute or null, if none of the namespaces has such
   *         an attribute defined.
   */
  public Object getFirstAttribute(final String attribute)
  {
    if (attribute == null)
    {
      throw new NullPointerException("Attribute name must not be null");
    }

    if (singletonContent != null)
    {
      final Object val = singletonContent.get(attribute);
      if (val != null)
      {
        return val;
      }
    }

    if (namespaces == null)
    {
      return null;
    }

    final Iterator entries = namespaces.entrySet().iterator();
    while (entries.hasNext())
    {
      final Map.Entry entry = (Map.Entry) entries.next();
      final HashMap map = (HashMap) entry.getValue();
      final Object val = map.get(attribute);
      if (val != null)
      {
        return val;
      }
    }
    return null;
  }

  /**
   * Returns all attributes of the given namespace as unmodifable map.
   *
   * @param namespace the namespace for which the attributes should be returned.
   * @return the map, never null.
   */
  public Map getAttributes(final String namespace)
  {
    if (namespace == null)
    {
      throw new NullPointerException("Attribute namespace must not be null");
    }

    if (namespace.equals(singletonNamespace))
    {
      return Collections.unmodifiableMap(singletonContent);
    }

    if (namespaces == null)
    {
      return Empty.MAP;
    }

    final HashMap attrs = (HashMap) namespaces.get(namespace);
    if (attrs == null)
    {
      return Empty.MAP;
    }
    else
    {
      return Collections.unmodifiableMap(attrs);
    }
  }

  /**
   * Returns all names for the given namespace that have values in this map.
   *
   * @param namespace the namespace for which known attribute names should be looked up.
   * @return the names stored for the given namespace.
   */
  public String[] getNames(final String namespace)
  {
    if (namespace == null)
    {
      throw new NullPointerException("Attribute namespace must not be null");
    }

    if (namespace.equals(singletonNamespace))
    {
      return (String[]) singletonContent.keySet().toArray(new String[singletonContent.size()]);
    }

    if (namespaces == null)
    {
      return AttributeMap.EMPTY_NAMESPACES;
    }

    final HashMap attrs = (HashMap) namespaces.get(namespace);
    if (attrs == null)
    {
      return AttributeMap.EMPTY_NAMESPACES;
    }
    else
    {
      return (String[]) attrs.keySet().toArray(new String[attrs.size()]);
    }
  }

  /**
   * Returns all namespaces that have values in this map.
   *
   * @return the namespaces stored in this map.
   */
  public String[] getNameSpaces()
  {
    if (namespaces == null)
    {
      if (singletonContent != null)
      {
        return new String[]{singletonNamespace};
      }
      return AttributeMap.EMPTY_NAMESPACES;
    }
    final String[] strings = (String[]) namespaces.keySet().toArray(new String[namespaces.size() + 1]);
    strings[strings.length - 1] = singletonNamespace;
    return strings;
  }

  public void putAll(final AttributeMap attributeMap)
  {
    final String[] namespaces = attributeMap.getNameSpaces();
    if (namespaces.length == 0)
    {
      return;
    }

    final boolean dontCopySingleton;
    if (this.singletonNamespace == null)
    {
      dontCopySingleton = true;
      this.singletonNamespace = attributeMap.singletonNamespace;
      this.singletonContent = (HashMap) attributeMap.singletonContent.clone();
    }
    else
    {
      if (this.singletonNamespace.equals(attributeMap.singletonNamespace))
      {
        dontCopySingleton = true;
        this.singletonContent.putAll(attributeMap.singletonContent);
      }
      else
      {
        dontCopySingleton = false;
      }
    }

    for (int i = 0; i < namespaces.length; i++)
    {
      final String namespace = namespaces[i];
      final Map sourceMap = attributeMap.getAttributes(namespace);
      if (dontCopySingleton && singletonNamespace.equals(namespace))
      {
        continue;
      }

      final HashMap targetMap = (HashMap) this.namespaces.get(namespace);
      if (targetMap == null)
      {
        this.namespaces.put(namespace, new HashMap(sourceMap));
      }
      else
      {
        targetMap.putAll(sourceMap);
      }
    }
  }

  public boolean equals(final Object o)
  {
    if (this == o)
    {
      return true;
    }
    if (o == null || getClass() != o.getClass())
    {
      return false;
    }

    final AttributeMap that = (AttributeMap) o;
    if (singletonNamespace != null ? !singletonNamespace.equals(that.singletonNamespace) : that.singletonNamespace != null)
    {
      return false;
    }
    if (singletonContent != null ? !singletonContent.equals(that.singletonContent) : that.singletonContent != null)
    {
      return false;
    }
    if (namespaces != null ? !namespaces.equals(that.namespaces) : that.namespaces != null)
    {
      return false;
    }

    return true;
  }

  public int hashCode()
  {
    int result = namespaces != null ? namespaces.hashCode() : 0;
    result = 31 * result + (singletonNamespace != null ? singletonNamespace.hashCode() : 0);
    result = 31 * result + (singletonContent != null ? singletonContent.hashCode() : 0);
    return result;
  }
}
