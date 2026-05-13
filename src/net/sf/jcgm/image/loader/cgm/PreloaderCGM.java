/*
 * Copyright (c) 2009, Swiss AviationSoftware Ltd. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 * - Neither the name of the Swiss AviationSoftware Ltd. nor the names of its
 *   contributors may be used to endorse or promote products derived from this
 *   software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.sf.jcgm.image.loader.cgm;

import java.io.IOException;

import javax.imageio.stream.ImageInputStream;
import javax.xml.transform.Source;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xmlgraphics.image.loader.ImageContext;
import org.apache.xmlgraphics.image.loader.ImageException;
import org.apache.xmlgraphics.image.loader.ImageInfo;
import org.apache.xmlgraphics.image.loader.ImageSize;
import org.apache.xmlgraphics.image.loader.impl.AbstractImagePreloader;
import org.apache.xmlgraphics.image.loader.util.ImageUtil;

import net.sf.jcgm.core.CGM;

/**
 * Preloader for CGM files.
 * <p>
 * Note: this class relies on the
 * {@link net.sf.jcgm.imageio.plugins.cgm.CGMImageReader} Image I/O plugin to
 * determine the image's size.
 * <p>
 * This implementation is largely inspired from the SVG Image preloader provided
 * with the XML graphics library.
 * 
 * @author xphc (Philippe Cadé)
 * @version $Id$
 * @since Apr 14, 2009
 * @see ImageLoaderCGM
 * @see <a href="http://xmlgraphics.apache.org/">xmlgraphics.apache.org</a>
 */
public class PreloaderCGM extends AbstractImagePreloader {
	
	protected static Log log = LogFactory.getLog("net.sf.jcgm.image.loader.cgm.PreloaderCGM");
	
	@Override
	public ImageInfo preloadImage(String originalURI, Source src, ImageContext context)
			throws ImageException, IOException {
        if (!ImageUtil.hasImageInputStream(src)) {
            return null;
        }
        
        ImageInputStream in = ImageUtil.needImageInputStream(src);
        IOException firstIOException = null;
        try {
            // Use the new readHeaderOnly feature to efficiently read just the CGM header
            CGM cgm = new CGM();
            in.mark();
            cgm.readHeader(in);
            in.reset();
            
            // Extract dimensions from the CGM header
            ImageSize size = new ImageSize();
            java.awt.Dimension cgmSize = cgm.getSize();
            if (cgmSize == null) {
                throw new ImageException("Could not extract image size from CGM header");
            }
            size.setSizeInPixels(cgmSize.width, cgmSize.height);
            
            // Set resolution from context and calculate size if needed
            size.setResolution(context.getSourceResolution());
            if (size.getWidthMpt() == 0) {
                size.calcSizeFromPixels();
            }
            
            // Create ImageInfo and store the input stream for later use by the loader
            ImageInfo info = new ImageInfo(originalURI, "image/cgm");
            info.getCustomObjects().put("InputStream", in);
            info.setSize(size);
            
            return info;
        } catch (IOException ioe) {
            firstIOException = ioe;
        } catch (NullPointerException e) {
            log.warn(originalURI + " " + e);
            return null;
        } finally {
            in.reset();
        }
        
        if (firstIOException != null) {
            throw new ImageException("I/O error while extracting image metadata"
                    + (firstIOException.getMessage() != null
                        ? ": " + firstIOException.getMessage()
                        : ""),
                    firstIOException);
        }
        
        return null;
	}

}
