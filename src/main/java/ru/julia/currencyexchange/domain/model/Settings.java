package ru.julia.currencyexchange.domain.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "settings")
public class Settings {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "conversion_percent")
    private double conversionFeePercent;

    @JsonBackReference
    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "preferred_currency_id")
    private Currency preferredCurrency;

    public Settings() {
    }

    public Settings(User user, Currency preferredCurrency) {
        this.user = user;
        this.preferredCurrency = preferredCurrency;
    }

    public String getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Currency getPreferredCurrency() {
        return preferredCurrency;
    }

    public void setPreferredCurrency(Currency preferredCurrency) {
        this.preferredCurrency = preferredCurrency;
    }

    public double getConversionFeePercent() {
        return conversionFeePercent;
    }

    public void setConversionFeePercent(double conversionFeePercent) {
        this.conversionFeePercent = conversionFeePercent;
    }
}
