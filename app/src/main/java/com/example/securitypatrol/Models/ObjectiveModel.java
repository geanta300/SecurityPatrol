package com.example.securitypatrol.Models;

import java.util.List;

public class ObjectiveModel {
        private int uniqueId;
        private String descriere;
        private String locatie;
        private String nfc_code;
        private int pompierID;

        private List<VerificationModel> verifications;

        public ObjectiveModel() {}

        public void setUniqueId(int uniqueId) {
                this.uniqueId = uniqueId;
        }

        public void setDescriere(String descriere) {
                this.descriere = descriere;
        }

        public void setLocatie(String locatie) {
                this.locatie = locatie;
        }

        public void setNfc_code(String nfc_code) {
                this.nfc_code = nfc_code;
        }

        public void setPompierID(int pompierID) {
                this.pompierID = pompierID;
        }

        public void setVerifications(List<VerificationModel> verifications) {
                this.verifications = verifications;
        }

        public int getUniqueId() {
                return uniqueId;
        }

        public String getDescriere() {
                return descriere;
        }

        public String getLocatie() {
                return locatie;
        }

        public String getNfc_code() {
                return nfc_code;
        }

        public int getPompierID() {
                return pompierID;
        }

        public List<VerificationModel> getVerifications() {
                return verifications;
        }
}
