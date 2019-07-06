package crawler.parsers.amazon.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * Created by romanb on 10/30/17.
 */

@Getter
@Setter
@NoArgsConstructor
public class Product {

    private Long id;
    private String shopUrl;
    private String asin;
    private Boolean isFbt;
    private String productName;
    private String category;
    private String categoryLink;
    private String mainCategory;
    private String brand;
    private Integer categoryRank;
    private Integer mainCategoryRank;
    private Double price;
    private Double rating;
    private Integer ratingCount;
    private String restFbt;
    private Integer fbtNumber;
    private String uniqueId;
    private Boolean isCurrentState;
    private String parentAsin;
    private String fulfillment;
    private String imageUrl;
    private Product parent;
    private List<Product> fbts;
    private Date createdAt;

}
