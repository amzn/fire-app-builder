/**
 * This file was modified by Amazon:
 * Copyright 2015-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
/*
 * Copyright (c) 2014, Nexage, Inc. All rights reserved.
 * Copyright (C) 2016 Amazon Inc.
 *
 * Provided under BSD-3 license as follows:
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 *  and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Nexage nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.amazon.android.ads.vast.model;

import com.amazon.android.ads.vast.util.VASTLog;
import com.amazon.android.ads.vast.util.XmlTools;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

public class VASTModel implements Serializable {

	private static String TAG = "VASTModel";

	private static final long serialVersionUID = 4318368258447283733L;

	private transient Document vastsDocument;
	private String pickedMediaFileURL = null;

	// Tracking xpath expressions
	private static final String inlineLinearTrackingXPATH = "/VASTS/VAST/Ad/InLine/Creatives/Creative/Linear/TrackingEvents/Tracking";
	private static final String inlineNonLinearTrackingXPATH = "/VASTS/VAST/Ad/InLine/Creatives/Creative/NonLinearAds/TrackingEvents/Tracking";
	private static final String wrapperLinearTrackingXPATH = "/VASTS/VAST/Ad/Wrapper/Creatives/Creative/Linear/TrackingEvents/Tracking";
	private static final String wrapperNonLinearTrackingXPATH = "/VASTS/VAST/Ad/Wrapper/Creatives/Creative/NonLinearAds/TrackingEvents/Tracking";

	private static final String combinedTrackingXPATH = inlineLinearTrackingXPATH
			+ "|"
			+ inlineNonLinearTrackingXPATH
			+ "|"
			+ wrapperLinearTrackingXPATH + "|" + wrapperNonLinearTrackingXPATH;

	// Mediafile xpath expression
	private static final String mediaFileXPATH = "//MediaFile";

	// Duration xpath expression
	private static final String durationXPATH = "//Duration";

	// Videoclicks xpath expression
	private static final String videoClicksXPATH = "//VideoClicks";

	// Videoclicks xpath expression
	private static final String impressionXPATH = "//Impression";
	
	// Error url  xpath expression
	private static final String errorUrlXPATH = "//Error";

	public VASTModel(Document vasts) {

		this.vastsDocument = vasts;

	}


	public Document getVastsDocument() {
		return vastsDocument;
	}

	public HashMap<TRACKING_EVENTS_TYPE, List<String>> getTrackingUrls() {
		VASTLog.d(TAG, "getTrackingUrls");

		List<String> tracking;

		HashMap<TRACKING_EVENTS_TYPE, List<String>> trackings = new HashMap<>();

		XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			NodeList nodes = (NodeList) xpath.evaluate(combinedTrackingXPATH,
					vastsDocument, XPathConstants.NODESET);
			Node node;
			String trackingURL;
			String eventName;
			TRACKING_EVENTS_TYPE key;

			if (nodes != null) {
				for (int i = 0; i < nodes.getLength(); i++) {
					node = nodes.item(i);
					NamedNodeMap attributes = node.getAttributes();

					eventName = (attributes.getNamedItem("event"))
							.getNodeValue();
					try {
						key = TRACKING_EVENTS_TYPE.valueOf(eventName);
					} catch (IllegalArgumentException e) {
						VASTLog.w(TAG, "Event:" + eventName
								+ " is not valid. Skipping it.");
						continue;
					}

					trackingURL = XmlTools.getElementValue(node);

					if (trackings.containsKey(key)) {
						tracking = trackings.get(key);
						tracking.add(trackingURL);
					} else {
						tracking = new ArrayList<>();
						tracking.add(trackingURL);
						trackings.put(key, tracking);

					}

				}
			}

		} catch (Exception e) {
			VASTLog.e(TAG, e.getMessage(), e);
			return null;
		}

		return trackings;
	}

	public List<VASTMediaFile> getMediaFiles() {
		VASTLog.d(TAG, "getMediaFiles");

		ArrayList<VASTMediaFile> mediaFiles = new ArrayList<>();

		XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			NodeList nodes = (NodeList) xpath.evaluate(mediaFileXPATH,
					vastsDocument, XPathConstants.NODESET);
			Node node;
			VASTMediaFile mediaFile;
			String mediaURL;
			Node attributeNode;

			if (nodes != null) {
				for (int i = 0; i < nodes.getLength(); i++) {
					mediaFile = new VASTMediaFile();
					node = nodes.item(i);
					NamedNodeMap attributes = node.getAttributes();	

					attributeNode = attributes.getNamedItem("apiFramework");
					mediaFile.setApiFramework((attributeNode == null) ? null
							: attributeNode.getNodeValue());

					attributeNode = attributes.getNamedItem("bitrate");
					mediaFile.setBitrate((attributeNode == null) ? null
							: new BigInteger(attributeNode.getNodeValue()));

					attributeNode = attributes.getNamedItem("delivery");
					mediaFile.setDelivery((attributeNode == null) ? null
							: attributeNode.getNodeValue());

					attributeNode = attributes.getNamedItem("height");
					mediaFile.setHeight((attributeNode == null) ? null
							: new BigInteger(attributeNode.getNodeValue()));

					attributeNode = attributes.getNamedItem("id");
					mediaFile.setId((attributeNode == null) ? null
							: attributeNode.getNodeValue());

					attributeNode = attributes
							.getNamedItem("maintainAspectRatio");
					mediaFile
							.setMaintainAspectRatio((attributeNode == null) ? null
									: Boolean.valueOf(attributeNode
											.getNodeValue()));

					attributeNode = attributes.getNamedItem("scalable");
					mediaFile.setScalable((attributeNode == null) ? null
							: Boolean.valueOf(attributeNode.getNodeValue()));

					attributeNode = attributes.getNamedItem("type");
					mediaFile.setType((attributeNode == null) ? null
							: attributeNode.getNodeValue());

					attributeNode = attributes.getNamedItem("width");
					mediaFile.setWidth((attributeNode == null) ? null
							: new BigInteger(attributeNode.getNodeValue()));

					mediaURL = XmlTools.getElementValue(node);
					mediaFile.setValue(mediaURL);

					mediaFiles.add(mediaFile);
				}
			}

		} catch (Exception e) {
			VASTLog.e(TAG, e.getMessage(), e);
			return null;
		}

		return mediaFiles;
	}

	public String getDuration() {
		VASTLog.d(TAG, "getDuration");

		String duration = null;

		XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			NodeList nodes = (NodeList) xpath.evaluate(durationXPATH,
					vastsDocument, XPathConstants.NODESET);
			Node node;

			if (nodes != null) {
				for (int i = 0; i < nodes.getLength(); i++) {
					node = nodes.item(i);
					duration = XmlTools.getElementValue(node);
				}
			}

		} catch (Exception e) {
			VASTLog.e(TAG, e.getMessage(), e);
			return null;
		}

		return duration;
	}

	public VideoClicks getVideoClicks() {
		VASTLog.d(TAG, "getVideoClicks");

		VideoClicks videoClicks = new VideoClicks();

		XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			NodeList nodes = (NodeList) xpath.evaluate(videoClicksXPATH,
					vastsDocument, XPathConstants.NODESET);
			Node node;

			if (nodes != null) {
				for (int i = 0; i < nodes.getLength(); i++) {
					node = nodes.item(i);

					NodeList childNodes = node.getChildNodes();

					Node child;
					String value;

					for (int childIndex = 0; childIndex < childNodes
							.getLength(); childIndex++) {
		
						child = childNodes.item(childIndex);
						String nodeName = child.getNodeName();

						if (nodeName.equalsIgnoreCase("ClickTracking")) {
							value = XmlTools.getElementValue(child);
							videoClicks.getClickTracking().add(value);

						} else if (nodeName.equalsIgnoreCase("ClickThrough")) {
							value = XmlTools.getElementValue(child);
							videoClicks.setClickThrough(value);

						} else if (nodeName.equalsIgnoreCase("CustomClick")) {
							value = XmlTools.getElementValue(child);
							videoClicks.getCustomClick().add(value);
						}
					}
				}
			}

		} catch (Exception e) {
			VASTLog.e(TAG, e.getMessage(), e);
			return null;
		}

		return videoClicks;
	}

	public List<String> getImpressions() {
		VASTLog.d(TAG, "getImpressions");

		return getListFromXPath(impressionXPATH);
	}

	public List<String>  getErrorUrl() {
		
		VASTLog.d(TAG, "getErrorUrl");

		return getListFromXPath(errorUrlXPATH);
	}
	
	
	
	private List<String>  getListFromXPath(String xPath) {
		
		VASTLog.d(TAG, "getListFromXPath");

		ArrayList<String> list = new ArrayList<>();
		
		XPath xpath = XPathFactory.newInstance().newXPath();

		try {
			NodeList nodes = (NodeList) xpath.evaluate(xPath,
					vastsDocument, XPathConstants.NODESET);
			Node node;

			if (nodes != null) {
				for (int i = 0; i < nodes.getLength(); i++) {
					node = nodes.item(i);
					list.add(XmlTools.getElementValue(node));
				}
			}

		} catch (Exception e) {		
			VASTLog.e(TAG, e.getMessage(), e);
			return null;
		}

		return list;
	
	}
	
	
	
	private void writeObject(ObjectOutputStream oos) throws IOException {
		VASTLog.d(TAG, "writeObject: about to write");
		oos.defaultWriteObject();

		String data = XmlTools.xmlDocumentToString(vastsDocument);
		// oos.writeChars();
		oos.writeObject(data);
		VASTLog.d(TAG, "done writing");

	}

	private void readObject(ObjectInputStream ois)
			throws ClassNotFoundException, IOException {
		VASTLog.d(TAG, "readObject: about to read");
		ois.defaultReadObject();

		String vastString = (String) ois.readObject();
		VASTLog.d(TAG, "vastString data is:\n" + vastString + "\n");

		vastsDocument = XmlTools.stringToDocument(vastString);

		VASTLog.d(TAG, "done reading");
	}

	public String getPickedMediaFileURL() {
		return pickedMediaFileURL;
	}

	public void setPickedMediaFileURL(String pickedMediaFileURL) {
		this.pickedMediaFileURL = pickedMediaFileURL;
	}

}
