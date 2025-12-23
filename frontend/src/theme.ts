import { createTheme } from '@mui/material/styles';


const baseTypography = {
  fontFamily: [
    'Inter',
    '-apple-system',
    'BlinkMacSystemFont',
    '"Segoe UI"',
    'Roboto',
    '"Helvetica Neue"',
    'Arial',
    'sans-serif',
  ].join(','),
  h1: { fontWeight: 700 },
  h2: { fontWeight: 700 },
  h3: { fontWeight: 700 },
  h4: { fontWeight: 700 },
  h5: { fontWeight: 600 },
  h6: { fontWeight: 600 },
  body1: { fontWeight: 400 },
  body2: { fontWeight: 400 },
  button: { fontWeight: 600, textTransform: 'none' }
};

const commonComponents = {
  MuiButton: {
    styleOverrides: {
      root: {
        borderRadius: 12,
        padding: '10px 18px',
        transition: 'transform 0.12s ease, box-shadow 0.12s ease',
      },
      contained: {
        boxShadow: '0 6px 18px rgba(16, 24, 40, 0.08)',
        '&:hover': {
          transform: 'translateY(-1px)',
          boxShadow: '0 8px 24px rgba(16, 24, 40, 0.12)'
        }
      }
    }
  },
  MuiCard: {
    styleOverrides: {
      root: {
        borderRadius: 14,
        boxShadow: '0 8px 24px rgba(16,24,40,0.06)',
        transition: 'transform 0.18s, box-shadow 0.18s',
        '&:hover': {
          transform: 'translateY(-4px)',
          boxShadow: '0 12px 40px rgba(16,24,40,0.08)',
        },
      }
    }
  },
  MuiPaper: {
    styleOverrides: {
      root: {
        backgroundImage: 'none',
      }
    }
  },
  MuiTextField: {
    styleOverrides: {
      root: {
        '& .MuiOutlinedInput-root': {
          borderRadius: 10,
          '&.Mui-focused fieldset': {
            boxShadow: '0 6px 18px rgba(25,118,210,0.06)',
          }
        }
      }
    }
  },
  MuiSelect: {
    styleOverrides: {
      root: {
        borderRadius: 10,
      },
      icon: {
        opacity: 0.9
      }
    }
  },
  MuiTableHead: {
    styleOverrides: {
      root: {
        '& .MuiTableCell-head': {
          fontWeight: 700,
        }
      }
    }
  },
  MuiTableRow: {
    styleOverrides: {
      root: {
        '&:nth-of-type(odd)': {
        },
        '&:hover': {
          transform: 'none'
        }
      }
    }
  },
  MuiTooltip: {
    styleOverrides: {
      tooltip: {
        borderRadius: 8,
        fontSize: '0.875rem'
      }
    }
  }
};

// Dark theme
export const darkTheme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#64b5f6',
      light: '#9be7ff',
      dark: '#218cbf',
      contrastText: '#ffffff'
    },
    secondary: {
      main: '#81c784',
      contrastText: '#fff'
    },
    background: {
      default: '#0f1416',
      paper: '#14181a'
    },
    text: {
      primary: '#e6eef6',
      secondary: '#b8c6d6'
    },
    error: { main: '#ef9a9a' },
    warning: { main: '#ffb74d' },
    info: { main: '#4fc3f7' },
    success: { main: '#81c784' }
  },
  shape: { borderRadius: 12 },
  typography: baseTypography,
  components: {
    ...commonComponents,
    MuiTableHead: {
      styleOverrides: {
        root: {
          backgroundColor: '#182022',
          '& .MuiTableCell-head': {
            fontWeight: 700,
            color: '#e6eef6'
          }
        }
      }
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          '&:nth-of-type(odd)': {
            backgroundColor: 'rgba(255,255,255,0.02)'
          },
          '&:hover': {
            backgroundColor: 'rgba(100,181,246,0.06)'
          }
        }
      }
    },
    MuiCssBaseline: {
      styleOverrides: {
        body: {
          WebkitFontSmoothing: 'antialiased',
          MozOsxFontSmoothing: 'grayscale'
        }
      }
    }
  }
});

export const lightTheme = createTheme({
  palette: {
    mode: 'light',
    primary: {
      main: '#0d6efd',
      light: '#5ea6ff',
      dark: '#0047b3',
      contrastText: '#ffffff'
    },
    secondary: {
      main: '#198754',
      light: '#5bd08e',
      dark: '#0b3e25',
      contrastText: '#ffffff'
    },
    background: {
      default: '#f4f7fb',
      paper: '#ffffff'
    },
    text: {
      primary: '#0b1b2b',
      secondary: '#556270'
    },
    error: { main: '#d32f2f' },
    warning: { main: '#ff9800' },
    info: { main: '#0288d1' },
    success: { main: '#2e7d32' }
  },
  shape: { borderRadius: 12 },
  typography: baseTypography,
  components: {
    ...commonComponents,
    MuiTableHead: {
      styleOverrides: {
        root: {
          backgroundColor: '#f1f5f9',
          '& .MuiTableCell-head': {
            fontWeight: 700,
            color: '#0b1b2b'
          }
        }
      }
    },
    MuiTableRow: {
      styleOverrides: {
        root: {
          '&:nth-of-type(odd)': {
            backgroundColor: 'rgba(11,27,43,0.02)'
          },
          '&:hover': {
            backgroundColor: 'rgba(13,110,253,0.06)'
          }
        }
      }
    },
    MuiCssBaseline: {
      styleOverrides: {
        body: {
          WebkitFontSmoothing: 'antialiased',
          MozOsxFontSmoothing: 'grayscale'
        }
      }
    }
  }
});
