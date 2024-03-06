package com.example.securitypatrol.Models;

import android.graphics.Bitmap;

public class GuardsSignatures {
    String guardName;
    Bitmap signatureImage;

    public GuardsSignatures(String guardName, Bitmap signatureImage) {
        this.guardName = guardName;
        this.signatureImage = signatureImage;
    }

    public String getGuardName() {
        return guardName;
    }

    public void setGuardName(String guardName) {
        this.guardName = guardName;
    }

    public Bitmap getSignatureImage() {
        return signatureImage;
    }

    public void setSignatureImage(Bitmap signatureImage) {
        this.signatureImage = signatureImage;
    }
}
