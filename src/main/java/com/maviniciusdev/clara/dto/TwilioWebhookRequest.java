package com.maviniciusdev.clara.dto;

import lombok.Data;

@Data
public class TwilioWebhookRequest {

    // Getters formatados
    // Campos básicos (já existentes)
    private String From;
    private String Body;
    private String ProfileName;

    // ⭐ NOVOS CAMPOS PARA MÍDIA
    private String NumMedia;           // Número de arquivos enviados
    private String MediaUrl0;          // URL do primeiro arquivo
    private String MediaContentType0;  // Tipo MIME (image/jpeg, application/pdf, etc)
    private String MediaUrl1;          // URL do segundo arquivo (se houver)
    private String MediaContentType1;

    public String getBody() {
        return Body != null ? Body : "";
    }

}
