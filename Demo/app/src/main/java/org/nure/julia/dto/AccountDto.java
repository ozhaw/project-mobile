package org.nure.julia.dto;

import org.nure.julia.mapper.SystemType;

import java.io.Serializable;

public class AccountDto implements Serializable {
    private Long userId;
    private String id;
    private String name;
    private String email;
    private String photoUri;
    private SystemType systemType;
    private String accessToken;

    public AccountDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public SystemType getSystemType() {
        return systemType;
    }

    public void setSystemType(SystemType systemType) {
        this.systemType = systemType;
    }

    public static AccountDtoBuilder builder() {
        return new AccountDtoBuilder();
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public static class AccountDtoBuilder {
        private AccountDto accountDto;

        private AccountDtoBuilder() {
            this.accountDto = new AccountDto();
        }

        public AccountDtoBuilder setId(String id) {
            this.accountDto.setId(id);
            return this;
        }

        public AccountDtoBuilder setName(String name) {
            this.accountDto.setName(name);
            return this;
        }

        public AccountDtoBuilder setEmail(String email) {
            this.accountDto.setEmail(email);
            return this;
        }

        public AccountDtoBuilder setPhotoUri(String photoUri) {
            this.accountDto.setPhotoUri(photoUri);
            return this;
        }

        public AccountDtoBuilder setSystemType(SystemType systemType) {
            this.accountDto.setSystemType(systemType);
            return this;
        }

        public AccountDto build() {
            return accountDto;
        }
    }
}
