package com.zenlambda.maven_un7z_plugin;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.ISevenZipInArchive;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;
import net.sf.sevenzipjbinding.simple.ISimpleInArchive;
import net.sf.sevenzipjbinding.simple.ISimpleInArchiveItem;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;



/**
 * @goal unpack
 * @requiresOnline false
 * @requiresProject false
 * @author Frank Wilson
 */
public class Un7zPlugin extends AbstractMojo {

	/**
	 * @parameter
	 */
	private String destDir;
	
	/**
	 * @parameter
	 */
	private String srcArchive;
	
	/**
	 * @parameter default-value="true"
	 */
	private String failIfNotFound;
	
	/**
	 * @parameter default-value="true"
	 */
	private String failOnError;
	
	/**
	 * @parameter default-value="false"
	 */
	private boolean overwrite;

	/**
	 * Executes this goal.
	 * @throws org.apache.maven.plugin.MojoExecutionException
	 */
	public void execute() throws MojoExecutionException {


		// Check other params.
		if( destDir == null || "".equals( destDir ) )
			handleError("destDir must be set.");
		if( srcArchive == null || "".equals( srcArchive ) )
			handleError("srcArchive must be set.");

		try {
			unzipArchive( srcArchive, destDir );
		}
		catch( FileNotFoundException ex ) {
			// Do not fail if parameter "failIfNotFound" is "false"
			handleError("false".equals(this.failIfNotFound), "Archive not found: "+ex.getMessage(), ex);
		}
		catch( IOException ex ) {
			handleError("IO error: "+ex.getMessage(), ex);
		}

	}

	/**
	 * Handles an error with the given string and throwable
	 * Exception is suppressed if failOnError = true.
	 * This override allows the configured error supression policy to be override and
	 * expects a throwable argument.
	 * 
	 * @param dontFail  Don't fail for this error, even if failOnError is true.
	 * @param msg the error string
	 * @param ex the throwable that has been caught or generated with this error
	 * @throws org.apache.maven.plugin.MojoExecutionException if this.failOnError != "false".
	 */
	private void handleError( boolean dontFail, String msg, Throwable ex ) throws MojoExecutionException {
		if( dontFail || "false".equals(this.failOnError))
			getLog().error( msg /*, ex*/ );
		else
			throw new MojoExecutionException( msg, ex );
	}
	
	/** 
	 * Handles an error with the given string and throwable
	 * Exception is suppressed if failOnError = true.
	 * This override expects a throwable argument
	 *  
	 * @param msg the error string
	 * @param ex the throwable that has been caught or generated with this error
	 */
	private void handleError( String msg, Throwable ex ) throws MojoExecutionException {
		handleError( false, msg, ex );
	}

	/** 
	 * Handles an error with the given string and throwable
	 * Exception is suppressed if failOnError = true.
	 * This override does not take a throwable argument
	 * 
	 * @param msg the error string 
	 */
	private void handleError( String msg ) throws MojoExecutionException {
		if( "false".equals(this.failOnError))
			getLog().error( msg );
		else
			throw new MojoExecutionException( msg );
	}

	/**
	 * Unzips archive from the given path to the given destination dir.
	 */
	private void unzipArchive(final String archivePath, final String destDir)
					throws FileNotFoundException, IOException
	{
		getLog().info("Extracting "+archivePath+" to "+destDir+"");
		
		// Create destination dir if it doesn't exists yet.
		(new File(destDir)).mkdirs();
		
        RandomAccessFile randomAccessFile = null;
        ISevenZipInArchive inArchive = null;
        try {
            randomAccessFile = new RandomAccessFile(archivePath, "r");
            inArchive = SevenZip.openInArchive(null, // autodetect archive type
                    new RandomAccessFileInStream(randomAccessFile));

            // Getting simple interface of the archive inArchive
            ISimpleInArchive simpleInArchive = inArchive.getSimpleInterface();

            getLog().debug("   Hash   |    Size    | Filename");
            getLog().debug("----------+------------+---------");
            
            boolean updateRequired = false;
            	
            for (final ISimpleInArchiveItem item : simpleInArchive.getArchiveItems()) {
                final int[] hash = new int[] { 0 };
                if (!item.isFolder()) {
                    ExtractOperationResult result;

                    File file = new File(destDir +"/"+ item.getPath());
                    
                    if (!overwrite && file.exists() && 
                    		item.getLastWriteTime() != null && 
                    		file.lastModified() >= item.getLastWriteTime().getTime()) {
                    	getLog().debug(String.format("skipping %s as it is up to date", 
                    			item.getPath()));
                    	continue;
                    } else {
                    	updateRequired = true;
                    }
                    
                    // not sure if parent dirs are extracted before children
                    // so make parent dir to be safe
					file.getParentFile().mkdirs();
                    
                    final FileOutputStream fos = new FileOutputStream(file);
                    
                    final long[] sizeArray = new long[1];
                    result = item.extractSlow(new ISequentialOutStream() {
                        public int write(byte[] data) throws SevenZipException {
                            hash[0] ^= Arrays.hashCode(data); // Consume data
                            sizeArray[0] += data.length;
                            
                            try {
                            	fos.write(data);
                            } catch(IOException e) {
                            	throw new SevenZipException("Error writing extracted file: "+item.getPath(),e);
                            }
                            
                            return data.length; // Return amount of consumed data
                        }
                    });
                    fos.close();
                    if (result == ExtractOperationResult.OK) {
                    	getLog().debug(String.format("%9X | %10s | %s", // 
                                hash[0], sizeArray[0], item.getPath()));
                    } else {
                        getLog().error("Error extracting item: " + result);
                    }
                } else {
    				getLog().debug("Extracting directory: " + item.getPath());
    				
    				File file = new File(destDir +"/"+ item.getPath());
					file.mkdirs();
    				if(!file.exists()) {
    					updateRequired = true;
    				}
                }
            }
            if(!updateRequired) {
            	getLog().info(String.format("no new paths found in archive %s for destination %s, no changes where made", 
            			archivePath, destDir));
            }
        } catch (Exception e) {
        	getLog().error("Error occurs: " + e);
        } finally {
            if (inArchive != null) {
                try {
                    inArchive.close();
                } catch (SevenZipException e) {
                	getLog().error("Error closing archive: " + e);
                }
            }
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                	getLog().error("Error closing file: " + e);
                }
            }
        }
		
	}

}
