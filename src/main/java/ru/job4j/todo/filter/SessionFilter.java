package ru.job4j.todo.filter;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import ru.job4j.todo.dto.CurrentUser;
import ru.job4j.todo.model.User;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Component
@Order(2)
public class SessionFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String uri = request.getRequestURI();
        HttpSession session = request.getSession(false);

        // Сбрасываем сессию при входе на регистрацию
        if (session != null && (uri.equals("/users/register") || uri.startsWith("/users/register/"))) {
            session.invalidate();
            session = null;
        }

        setCurrentUserToRequest(session, request);
        chain.doFilter(request, response);
    }

    private void setCurrentUserToRequest(HttpSession session, HttpServletRequest request) {
        User userFromSession = null;
        if (session != null) {
            Object attr = session.getAttribute("user");
            if (attr instanceof User) {
                userFromSession = (User) attr;
            }
        }

        CurrentUser currentUser = (userFromSession != null)
                ? CurrentUser.of(userFromSession)
                : CurrentUser.guest();

        request.setAttribute("user", currentUser);
    }
}