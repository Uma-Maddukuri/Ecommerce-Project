package com.datastax.tutorials.service.price;

import java.math.BigDecimal;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Price {
	
	@Size(min=5, max=30)
    @NotNull(message="ProductId cannot be null")
	@JsonProperty("product_id")
    private String productId;
	
	@JsonProperty("store_id")
	@NotNull(message="StoreId cannot be null")
	private String storeId;
	
	@Min(value=0, message="Price must be positive")
	@NotNull(message="Value cannot be null")
	private BigDecimal value;

	public Price(@Size(min = 5, max = 30) 
	             @NotNull(message = "ProductId cannot be null") String productId,
	             @NotNull(message = "StoreId cannot be null") String storeId,
	             @Min(value = 0, message = "Price must be positive") 
	             @NotNull(message = "Value cannot be null") BigDecimal value) {
        super();
        this.productId = productId;
        this.storeId = storeId;
        this.value = value;
    }

    public Price() {}

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
	
	
}
