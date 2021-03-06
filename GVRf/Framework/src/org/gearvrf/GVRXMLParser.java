/* Copyright 2015 Samsung Electronics Co., LTD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gearvrf;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.gearvrf.utility.VrAppSettings;
import org.gearvrf.utility.VrAppSettings.EyeBufferParms.ColorFormat;
import org.gearvrf.utility.VrAppSettings.EyeBufferParms.DepthFormat;
import org.gearvrf.utility.VrAppSettings.EyeBufferParms.TextureFilter;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.res.AssetManager;
import android.util.Log;

/**
 * This class simply parses XML file for distortion stored in assets folder, and
 * allows users to read specific distortion value from the XML file.
 * {@link GVRViewManager} calls GVRXMLParser to initialize the distortion value
 * internally
 */
class GVRXMLParser {
    private float mLensSeparationDistance = 0.0f;
    private float mFovY = 0.0f;
    private int mFBOWidth = 512;
    private int mFBOHeight = 512;
    private int mMSAA = 1;

    /**
     * Constructs a GVRXMLParser with current package assets manager and the
     * file name of the distortion xml file under assets folder
     * 
     * @param assets
     *            the current {@link AssetManager} for the package
     * @param fileName
     *            the distortion file name under assets folder
     */
    GVRXMLParser(AssetManager assets, String fileName, VrAppSettings settings) {
        try {
            StringBuilder buf = new StringBuilder();
            InputStream is = assets.open(fileName);
            BufferedReader in = new BufferedReader(new InputStreamReader(is));

            String str;
            boolean isFirst = true;
            while ((str = in.readLine()) != null) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    buf.append('\n');
                }
                buf.append(str);
            }

            String file = buf.toString();

            in.close();
            is.close();

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser xpp = factory.newPullParser();

            xpp.setInput(new StringReader(file));
            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                } else if (eventType == XmlPullParser.START_TAG) {
                    String tagName = xpp.getName();
                    for (int i = 0; i < xpp.getAttributeCount(); ++i) {
                        String attributeName = xpp.getAttributeName(i);
                        if (tagName.equals("metric")) {
                            if (attributeName
                                    .equals("lens-separation-distance")) {
                                mLensSeparationDistance = Float.parseFloat(xpp
                                        .getAttributeValue(i));
                            }
                        } else if (tagName.equals("scene")) {
                            if (attributeName.equals("fov-y")) {
                                mFovY = Float.parseFloat(xpp
                                        .getAttributeValue(i));
                            } else if (attributeName.equals("fbo-width")) {
                                mFBOWidth = Integer.parseInt(xpp
                                        .getAttributeValue(i));
                            } else if (attributeName.equals("fbo-height")) {
                                mFBOHeight = Integer.parseInt(xpp
                                        .getAttributeValue(i));
                            } else if (attributeName.equals("msaa")) {
                                mMSAA = Integer.parseInt(xpp
                                        .getAttributeValue(i));
                            }
                        } else if (tagName.equals("vr-app-settings")) {
                            Log.d("XMLParse", tagName + "   " + attributeName);
                            if(attributeName.equals("showLoadingIcon")){
                                settings.setShowLoadingIcon(Boolean.parseBoolean(xpp.getAttributeValue(i)));
                            }else if(attributeName.equals("renderMonoMode")){
                                settings.setRenderMonoMode(Boolean.parseBoolean(xpp.getAttributeValue(i)));
                            }else if(attributeName.equals("useSrgbFramebuffer")){
                                settings.setUseSrgbFramebuffer(Boolean.parseBoolean(xpp.getAttributeValue(i)));
                            }else if(attributeName.equals("useProtectedFramebuffer")){
                                settings.setUseProtectedFramebuffer(Boolean.parseBoolean(xpp.getAttributeValue(i)));
                            }else if(attributeName.equals("framebufferPixelsWide")){
                                settings.setFramebufferPixelsWide(Integer.parseInt(xpp.getAttributeValue(i)));
                            }else if(attributeName.equals("framebufferPixelsHigh")){
                                settings.setFramebufferPixelsHigh(Integer.parseInt(xpp.getAttributeValue(i)));
                            }
                        }else if(tagName.equals("mode-parms")){
                            if(attributeName.equals("allowPowerSave")){
                                settings.getModeParms().setAllowPowerSave(Boolean.parseBoolean(xpp.getAttributeValue(i)));
                            }else if(attributeName.equals("resetWindowFullScreen")){
                                settings.getModeParms().setResetWindowFullScreen(Boolean.parseBoolean(xpp.getAttributeValue(i)));
                            }else if(attributeName.equals("cpuLevel")){
                                settings.getModeParms().setCpuLevel(Integer.parseInt(xpp.getAttributeValue(i)));                                
                            }else if(attributeName.equals("gpuLevel")){
                                settings.getModeParms().setGpuLevel(Integer.parseInt(xpp.getAttributeValue(i)));
                            }
                        }else if(tagName.equals("eye-buffer-parms")){
                            String attributeValue = xpp.getAttributeValue(i);
                            if(attributeName.equals("depthFormat")){
                                if(attributeValue.equals("DEPTH_0")){
                                    settings.eyeBufferParms.setDepthFormat(DepthFormat.DEPTH_0);
                                }else if(attributeValue.equals("DEPTH_16")){
                                    settings.eyeBufferParms.setDepthFormat(DepthFormat.DEPTH_16);
                                }else if(attributeValue.equals("DEPTH_24")){
                                    settings.eyeBufferParms.setDepthFormat(DepthFormat.DEPTH_24);
                                }else if(attributeValue.equals("DEPTH_24_STENCIL_8")){
                                    settings.eyeBufferParms.setDepthFormat(DepthFormat.DEPTH_24_STENCIL_8);                                    
                                }
                            }else if(attributeName.equals("textureFilter")){
                                if(attributeValue.equals("TEXTURE_FILTER_NEAREST")){
                                    settings.eyeBufferParms.setTextureFilter(TextureFilter.TEXTURE_FILTER_NEAREST);
                                }else if(attributeValue.equals("TEXTURE_FILTER_BILINEAR")){
                                    settings.eyeBufferParms.setTextureFilter(TextureFilter.TEXTURE_FILTER_BILINEAR);
                                }else if(attributeValue.equals("TEXTURE_FILTER_ANISO_2")){
                                    settings.eyeBufferParms.setTextureFilter(TextureFilter.TEXTURE_FILTER_ANISO_2);
                                }else if(attributeValue.equals("TEXTURE_FILTER_ANISO_4")){
                                    settings.eyeBufferParms.setTextureFilter(TextureFilter.TEXTURE_FILTER_ANISO_4);
                                }
                            }else if(attributeName.equals("colorFormat")){
                                if(attributeValue.equals("COLOR_565")){
                                    settings.eyeBufferParms.setColorFormat(ColorFormat.COLOR_565);
                                }else if(attributeValue.equals("COLOR_5551")){
                                    settings.eyeBufferParms.setColorFormat(ColorFormat.COLOR_5551);
                                }else if(attributeValue.equals("COLOR_4444")){
                                    settings.eyeBufferParms.setColorFormat(ColorFormat.COLOR_4444);
                                }else if(attributeValue.equals("COLOR_8888")){
                                    settings.eyeBufferParms.setColorFormat(ColorFormat.COLOR_8888);
                                }else if(attributeValue.equals("COLOR_8888_sRGB")){
                                    settings.eyeBufferParms.setColorFormat(ColorFormat.COLOR_8888_sRGB);
                                }
                            }else if(attributeName.equals("multiSamples")){
                                settings.eyeBufferParms.setMultiSamples(Integer.parseInt(attributeValue));
                            }else if(attributeName.equals("widthScale")){
                                settings.eyeBufferParms.setWidthScale(Integer.parseInt(attributeValue));
                            }else if(attributeName.equals("resolution")){
                                settings.eyeBufferParms.setResolution(Integer.parseInt(attributeValue));
                            }
                        }else if(tagName.equals("head-model-parms")){
                            if(attributeName.equals("interpupillaryDistance")){
                                settings.headModelParms.setInterpupillaryDistance(Float.parseFloat(xpp.getAttributeValue(i)));
                            }else if(attributeName.equals("eyeHeight")){
                                settings.headModelParms.setEyeHeight(Float.parseFloat(xpp.getAttributeValue(i)));
                            }else if(attributeName.equals("headModelDepth")){
                                settings.headModelParms.setHeadModelDepth(Float.parseFloat(xpp.getAttributeValue(i)));
                            }else if(attributeName.equals("headModelHeight")){
                                settings.headModelParms.setHeadModelHeight(Float.parseFloat(xpp.getAttributeValue(i)));
                            }
                        }
                    }
                } else if (eventType == XmlPullParser.END_TAG) {
                } else if (eventType == XmlPullParser.TEXT) {
                }
                eventType = xpp.next();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e){
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Returns metric lens separation distance value
     * 
     * @return lens separation distance in float
     */
    float getLensSeparationDistance() {
        return mLensSeparationDistance;
    }

    /**
     * Returns scene fov-y value
     * 
     * @return fov-y in float
     */
    float getFovY() {
        return mFovY;
    }

    /**
     * Returns scene fbo-width value
     * 
     * @return fbo-width in int
     */
    int getFBOWidth() {
        return mFBOWidth;
    }

    /**
     * Returns scene fbo-height value
     * 
     * @return fbo-height in int
     */
    int getFBOHeight() {
        return mFBOHeight;
    }

    /**
     * Returns scene msaa value
     * 
     * @return msaa in int
     */
    int getMSAA() {
        return mMSAA;
    }
}
