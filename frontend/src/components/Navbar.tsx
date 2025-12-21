import React from 'react';
import {
  AppBar, Toolbar, Typography, Button, Box, IconButton, Drawer,
  List, ListItem, ListItemText, ListItemIcon, useMediaQuery
} from '@mui/material';
import {
  Menu as MenuIcon, Dashboard as DashboardIcon, Functions as FunctionsIcon,
  Science as ScienceIcon, Settings as SettingsIcon, ExitToApp as ExitToAppIcon,
  Add as AddIcon
} from '@mui/icons-material';
import { Link, useNavigate } from 'react-router-dom';
import { useTheme } from '@mui/material/styles';

interface NavbarProps {
  onLogout: () => void;
}

const Navbar: React.FC<NavbarProps> = ({ onLogout }) => {
  const [mobileOpen, setMobileOpen] = React.useState(false);
  const navigate = useNavigate();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));

  const handleDrawerToggle = () => {
    setMobileOpen(!mobileOpen);
  };

  const drawer = (
    <Box onClick={handleDrawerToggle} sx={{ textAlign: 'center' }}>
      <Typography variant="h6" sx={{ my: 2, fontWeight: 'bold' }}>
        Function Manager
      </Typography>
      <List>
        <ListItem button component={Link} to="/dashboard">
          <ListItemIcon>
            <DashboardIcon color="primary" />
          </ListItemIcon>
          <ListItemText primary="Dashboard" />
        </ListItem>
        <ListItem button component={Link} to="/functions/new">
          <ListItemIcon>
            <AddIcon color="primary" />
          </ListItemIcon>
          <ListItemText primary="Создать функцию" />
        </ListItem>
        <ListItem button component={Link} to="/operations">
          <ListItemIcon>
            <ScienceIcon color="primary" />
          </ListItemIcon>
          <ListItemText primary="Операции" />
        </ListItem>
        <ListItem button component={Link} to="/settings">
          <ListItemIcon>
            <SettingsIcon color="primary" />
          </ListItemIcon>
          <ListItemText primary="Настройки" />
        </ListItem>
        <ListItem button onClick={onLogout}>
          <ListItemIcon>
            <ExitToAppIcon color="error" />
          </ListItemIcon>
          <ListItemText primary="Выйти" primaryTypographyProps={{ color: 'error' }} />
        </ListItem>
      </List>
    </Box>
  );

  return (
    <AppBar position="fixed" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
      <Toolbar>
        <IconButton
          color="inherit"
          aria-label="open drawer"
          edge="start"
          onClick={handleDrawerToggle}
          sx={{ mr: 2, display: { sm: 'none' } }}
        >
          <MenuIcon />
        </IconButton>
        <Typography variant="h6" component="div" sx={{ flexGrow: 1, fontWeight: 'bold' }}>
          Function Manager
        </Typography>
        <Box sx={{ display: { xs: 'none', sm: 'block' } }}>
          <Button color="inherit" component={Link} to="/dashboard">Dashboard</Button>
          <Button color="inherit" component={Link} to="/functions/new">Создать функцию</Button>
          <Button color="inherit" component={Link} to="/operations">Операции</Button>
          <Button color="inherit" component={Link} to="/settings">Настройки</Button>
          <Button color="inherit" onClick={onLogout}>Выйти</Button>
        </Box>
      </Toolbar>
      <Drawer
        variant="temporary"
        open={mobileOpen}
        onClose={handleDrawerToggle}
        ModalProps={{
          keepMounted: true,
        }}
        sx={{
          display: { xs: 'block', sm: 'none' },
          '& .MuiDrawer-paper': { boxSizing: 'border-box', width: 240, bgcolor: 'background.paper' },
        }}
      >
        {drawer}
      </Drawer>
    </AppBar>
  );
};

export default Navbar;