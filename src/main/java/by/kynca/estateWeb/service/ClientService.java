package by.kynca.estateWeb.service;

import by.kynca.estateWeb.entity.Client;
import by.kynca.estateWeb.entity.Role;
import by.kynca.estateWeb.repository.ClientRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class ClientService implements UserDetailsService, ServiceActions<Client> {
    @Value("${page.size.client}")
    private int pageSize;

    private final ClientRepo clientRepo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Autowired
    public ClientService(ClientRepo clientRepo) {
        this.clientRepo = clientRepo;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return clientRepo.findClientByEmail(username).orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Override
    public Client save(Client client) {
        boolean exist = clientRepo.findClientByEmail(client.getEmail()).isPresent();

        if (exist) {
            return null;
        }

        client.setRole(Role.USER);
        client.setPassword(encoder.encode(client.getPassword()));
        client.setEnabled(true);
        return clientRepo.save(client);
    }

    @Override
    public List<Client> findAll(int page, String sort) {
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sort).descending());
        return clientRepo.findAllByRoleNot(Role.ADMIN, pageable).getContent();
    }

    public void setEnable(Long id) {
        Client client = clientRepo.findById(id).orElse(null);
        if (client != null && client.getRole() != Role.ADMIN) {
            client.setEnabled(!client.isEnabled());
        } else {
            return;
        }
        clientRepo.save(client);
    }

    @Override
    public Client findById(Long id) {
        return clientRepo.findById(id).orElse(null);
    }

}
