package ociautoscaling.common;

import com.google.common.base.Supplier;
import com.oracle.bmc.ClientConfiguration;
import com.oracle.bmc.ConfigFileReader;
import com.oracle.bmc.auth.SimpleAuthenticationDetailsProvider;
import com.oracle.bmc.auth.SimplePrivateKeySupplier;

import java.io.*;

/**
 * Static configuration settings.
 * @author guanglei
 *
 */
public class Config {
	

	/**
	 * Get auth provider from default configuration file. Profile sensitive.
	 * @param profile
	 * @return
	 * @throws IOException
	 */
	public static SimpleAuthenticationDetailsProvider getAuthProvider(String profile) throws IOException{
		
		ConfigFileReader.ConfigFile cf = ConfigFileReader.parse(ConfigFileReader.DEFAULT_FILE_PATH, profile.toUpperCase());
		Supplier<InputStream> ks = new SimplePrivateKeySupplier(cf.get("key_file"));
		System.out.println(cf.get("tenancy")+"\n"+cf.get("user")+"\n"+cf.get("fingerprint")+"\n"+ks.toString());
		return SimpleAuthenticationDetailsProvider.builder()
				.tenantId(cf.get("tenancy"))
				.userId(cf.get("user"))
				.fingerprint(cf.get("fingerprint"))
				.privateKeySupplier(ks).build();
	}
	
	/**
	 * Get configuration file reader for default configuration file location.
	 * @param profile
	 * @return
	 * @throws IOException
	 */
	public static ConfigFileReader.ConfigFile getConfigFileReader(String profile) throws IOException{
		return ConfigFileReader.parse(ConfigFileReader.DEFAULT_FILE_PATH, profile.toUpperCase());
	}
	
	/**
	 * A chance to change the default client configuration.
	 * @return
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public static ClientConfiguration getClientConfig(String profile) throws NumberFormatException, IOException{
		int ct = Integer.valueOf(Config.getConfigFileReader(profile).get("connectiontimeout"));
		int rt = Integer.valueOf(Config.getConfigFileReader(profile).get("readtimeout"));
		return (ClientConfiguration.builder().connectionTimeoutMillis(ct)
		.readTimeoutMillis(rt).build());
	}
	
	private static String getConf(String confKey, String profile) throws IOException{
		ConfigFileReader.ConfigFile cf = ConfigFileReader.parse(ConfigFileReader.DEFAULT_FILE_PATH, profile.toUpperCase());
		return cf.get(confKey);
	}
	
	/**
	 * Extract default compartmentId from default configuration file. The key must be 'compartment'.
	 * @param profile
	 * @return
	 * @throws IOException
	 */
	public static String getMyCompartmentId(String profile) throws IOException{
		return getConf("compartment", profile);
	}
	
	/**
	 * Extract current oracle linux image from default configuration file. The key must be "current_ol_image".
	 * @param profile
	 * @return
	 * @throws IOException
	 */
	public static String getCurrentOracleLinuxImage(String profile) throws IOException{
		return getConf("current_ol_image", profile);
	}

}
