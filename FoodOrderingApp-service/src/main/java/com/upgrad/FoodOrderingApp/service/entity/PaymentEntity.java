package com.upgrad.FoodOrderingApp.service.entity;


import javax.persistence.*;
import javax.validation.constraints.Size;
import java.io.Serializable;

@Entity
@Table(
        name = "payment"
)
@NamedQueries({
        @NamedQuery(name = "allPaymentMethods", query = "select p from PaymentEntity p ")
})
public class PaymentEntity implements Serializable {

    @Id
    @Column(
            name = "ID"
    )
    @GeneratedValue(
            strategy = GenerationType.IDENTITY
    )
    private long id;
    //uuid must be UNIQUE & NOTNULL
    @Column(
            name = "UUID"
    )
    @Size(
            max = 200
    )
    private String uuid;

    @Column(
            name = "PAYMENT_NAME"
    )
    //paymentName can be NULL
    private String paymentName;

    public long getId() {
        return id;
    }

    public void setId(long id) {
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Entity
@Table(name = "payment")
@NamedQueries(
        {
                @NamedQuery(name = "paymentByUuid", query = "select p from PaymentEntity p where p.uuid = :paymentUuid")
        }
)
public class PaymentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    private Integer id;

    @Column(name = "uuid")
    @NotNull
    @Size(max = 200)
    private String uuid;

    @Column(name = "payment_name")
    @NotNull
    @Size(max = 255)
    private String paymentName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getPaymentName() {
        return paymentName;
    }

    public void setPaymentName(String paymentName) {
        this.paymentName = paymentName;
    }

    @Override
    public boolean equals(Object obj) {
        return new EqualsBuilder().append(this, obj).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this).hashCode();
    }
}
