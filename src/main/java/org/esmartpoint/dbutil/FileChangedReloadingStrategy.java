package org.esmartpoint.dbutil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;

/**
 * <p>A reloading strategy that will reload the configuration every time its
 * underlying file is changed.</p>
 * <p>This reloading strategy does not actively monitor a configuration file,
 * but is triggered by its associated configuration whenever properties are
 * accessed. It then checks the configuration file's last modification date
 * and causes a reload if this has changed.</p>
 * <p>To avoid permanent disc access on successive property lookups a refresh
 * delay can be specified. This has the effect that the configuration file's
 * last modification date is only checked once in this delay period. The default
 * value for this refresh delay is 5 seconds.</p>
 * <p>This strategy only works with FileConfiguration instances.</p>
 *
 * @author Emmanuel Bourg
 * @version $Revision: 606798 $, $Date: 2007-12-25 20:05:58 +0100 (Di, 25 Dez 2007) $
 * @since 1.1
 */
public class FileChangedReloadingStrategy 
{
    /** Constant for the default refresh delay.*/
    private static final int DEFAULT_REFRESH_DELAY = 1000;

	static HashMap<String, FileChangedReloadingStrategy> reloadingCache = new HashMap<String, FileChangedReloadingStrategy>();
	
    /** Stores a reference to the configuration to be monitored.*/
    protected File file;

    /** The last time the configuration file was modified. */
    protected long lastModified;

    /** The last time the file was checked for changes. */
    protected long lastChecked;

    /** The minimum delay in milliseconds between checks. */
    protected long refreshDelay = DEFAULT_REFRESH_DELAY;

    /** A flag whether a reload is required.*/
    private boolean reloading;

    public void setFile(File configuration)
    {
        this.file = configuration;
    }

    public void init()
    {
        updateLastModified();
    }

    public boolean reloadingRequired()
    {
        if (!reloading)
        {
            long now = System.currentTimeMillis();

            if (now > lastChecked + refreshDelay)
            {
                lastChecked = now;
                if (hasChanged())
                {
                    reloading = true;
                }
            }
        }

        return reloading;
    }

    public void reloadingPerformed()
    {
        updateLastModified();
    }

    /**
     * Return the minimal time in milliseconds between two reloadings.
     *
     * @return the refresh delay (in milliseconds)
     */
    public long getRefreshDelay()
    {
        return refreshDelay;
    }

    /**
     * Set the minimal time between two reloadings.
     *
     * @param refreshDelay refresh delay in milliseconds
     */
    public void setRefreshDelay(long refreshDelay)
    {
        this.refreshDelay = refreshDelay;
    }

    /**
     * Update the last modified time.
     */
    protected void updateLastModified()
    {
        File file = getFile();
        if (file != null)
        {
            lastModified = file.lastModified();
        }
        reloading = false;
    }

    /**
     * Check if the configuration has changed since the last time it was loaded.
     *
     * @return a flag whether the configuration has changed
     */
    protected boolean hasChanged()
    {
        File file = getFile();
        if (file == null || !file.exists())
        {
            return false;
        }

        return file.lastModified() > lastModified;
    }

    /**
     * Returns the file that is monitored by this strategy. Note that the return
     * value can be <b>null </b> under some circumstances.
     *
     * @return the monitored file
     */
    protected File getFile()
    {
        return file;
    }
    
	static boolean reloadingRequired(String filename) throws MalformedURLException, IOException {
		if (filename.startsWith("CLASSPATH://")) {
			return false;
		} else if (filename.startsWith("HTTP://") || filename.startsWith("HTTPS://") || filename.startsWith("FTP://")) { 
			return false;
		} else {
			FileChangedReloadingStrategy reloadingStrategy = reloadingCache.get(filename);
			return reloadingStrategy == null || reloadingStrategy.reloadingRequired();
		}
	}
}
