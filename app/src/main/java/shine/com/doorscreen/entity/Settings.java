package shine.com.doorscreen.entity;

import java.util.List;

public  class Settings {

        private List<SystemLight> systemlight;
        private List<SystemVolume> systemvolume;

        public List<SystemVolume> getSystemvolume() {
            return systemvolume;
        }

        public List<SystemLight> getSystemlight() {
            return systemlight;
        }

    }
