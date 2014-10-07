//
// This path was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, vJAXB 2.1.10 in JDK 6
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this path will be lost upon recompilation of the source schema.
// Generated on: 2010.06.14 at 12:38:27 PM CEST 
//


package org.opencb.biodata.formats.protein.uniprot.v135jaxb;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for anonymous complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://uniprot.org/uniprot}entry" maxOccurs="unbounded"/>
 *         &lt;element ref="{http://uniprot.org/uniprot}copyright" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
        "entry",
        "copyright"
})
@XmlRootElement(name = "uniprot")
public class Uniprot {

    @XmlElement(required = true)
    protected List<Entry> entry;
    protected String copyright;

    /**
     * Gets the value of the entry property.
     * <p/>
     * <p/>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the entry property.
     * <p/>
     * <p/>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getEntry().add(newItem);
     * </pre>
     * <p/>
     * <p/>
     * <p/>
     * Objects of the following type(s) are allowed in the list
     * {@link Protein }
     */
    public List<Entry> getEntry() {
        if (entry == null) {
            entry = new ArrayList<Entry>();
        }
        return this.entry;
    }

    /**
     * Gets the value of the copyright property.
     *
     * @return possible object is
     *         {@link String }
     */
    public String getCopyright() {
        return copyright;
    }

    /**
     * Sets the value of the copyright property.
     *
     * @param value allowed object is
     *              {@link String }
     */
    public void setCopyright(String value) {
        this.copyright = value;
    }

}
