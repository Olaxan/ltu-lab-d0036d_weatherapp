package weatherapp;

import weatherapp.Locality;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class WeatherAppModel
{
	
	public static Locality[] ReadPlaces(String path)
	{
		
		List<Locality> places = new ArrayList<Locality>();
		
		try
		{
			File fXmlFile = new File(path);
			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
			Document doc = dBuilder.parse(fXmlFile);
			doc.getDocumentElement().normalize();

			System.out.println("Root element :" + doc.getDocumentElement().getNodeName());
					
			NodeList nList = doc.getElementsByTagName("locality");
					
			System.out.println("----------------------------");

			for (int temp = 0; temp < nList.getLength(); temp++)
			{
				
				String name;
				float latitude = 0, longitude = 0, altitude = 0;

				Node nNode = nList.item(temp);
						
				if (nNode.getNodeType() == Node.ELEMENT_NODE)
				{

					Element eElement = (Element) nNode;
					name = eElement.getAttribute("name");
					
					if (eElement.hasChildNodes())
					{
						NodeList nLocList = nNode.getChildNodes();
						Node nLocation = null;
						
						for (int i = 0; i < nLocList.getLength(); i++)
						{
							Node n = nLocList.item(i);
							
							if (n.getNodeType() == Node.ELEMENT_NODE)
							{
								nLocation = n;
								break;
							}
						}
						
						if (nLocation != null) // FUCKK
						{
							NamedNodeMap nLocAttrib = nLocation.getAttributes();
							
							try
							{
								latitude = Float.parseFloat(nLocAttrib.getNamedItem("latitude").getNodeValue());
								longitude = Float.parseFloat(nLocAttrib.getNamedItem("longitude").getNodeValue());
								altitude = Float.parseFloat(nLocAttrib.getNamedItem("altitude").getNodeValue());
							}
							catch (NumberFormatException e)
							{
								System.out.println("Error parsing places.xml\n" + e.getMessage());
							}
						}					
						
						Locality loc = new Locality(name, latitude, longitude, altitude);
						places.add(loc);
						
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
		return places.toArray(new Locality[places.size()]);
	}
}
