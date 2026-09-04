# epims-client
Activity, Sample and Instrument Management GUI Application (Using  Java Swing)

Previously hosted on CEA Tuleap Projects.

**TODO**
* create generic doc


## Revisions

### version 2.8.x

* Add better mgf management: fixes some error + allow user to specify acquisition to associate mgf to
* Add "Category" in Study (Sample and Acquisition) to distinguish between Research, QC, Service or Control studies
* Remove "Analysis Request" management until the new implementation
* Fix error on the data cache which needed server reboot
* Allow more FTP configuration (port/authentification mode)
* Modify Build to use batch instead of exe
* update to Java 17 & dependencies 
* Add config with the client-side config file (`epims-client.properties`) and where `epims-id_rsa` private key for FTP should be.

## Client configuration (config folder)

- A `config` folder is present in the distribution root
- Template file: `config/epims-client.properties`.
- Private key: `config/epims-id_rsa` (NOT committed to Git).
- Supported properties:
  - `ftp.host` — override server host if set.
  - `ftp.port` — optional SFTP port (defaults to 22 if not set). 
  - `ftp.keyPath` — path to private key. Defaults to `./config/epims-id_rsa`.


### version 2.4.x to 2.7.x

* Add "Analysis Request" management
* Allow mgf upload on ePims

### version 2.0 to 2.3.x

* New ePims client application (replace eP-Web...)
* FTP access to download files
* Plate management
* Bug fixes 
* Access to new ePims Server