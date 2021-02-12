package tacos.data;

import org.springframework.data.repository.CrudRepository;
import java.util.List;
import tacos.Order;
import tacos.User;
import org.springframework.data.domain.Pageable;
public interface OrderRepository extends CrudRepository<Order, Long>{
	List<Order> findByUserOrderByPlacedAtDesc(User user,Pageable pageable);
}
