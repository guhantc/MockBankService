import React, { useState } from 'react';
import { motion } from 'framer-motion';
import { 
  ChevronDownIcon, 
  UserIcon, 
  MagnifyingGlassIcon,
  PhoneIcon,
  MapPinIcon,
  BanknotesIcon,
  CreditCardIcon,
  ShieldCheckIcon,
  ChartBarIcon,
  ComputerDesktopIcon,
  BuildingOfficeIcon,
  Bars3Icon,
  XMarkIcon
} from '@heroicons/react/24/outline';

// Header Component
const Header = () => {
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [activeDropdown, setActiveDropdown] = useState(null);

  const navigationItems = [
    {
      name: 'Personal Banking',
      dropdown: [
        'Savings Account',
        'Current Account',
        'Fixed Deposits',
        'Recurring Deposits',
        'Digital Banking'
      ]
    },
    {
      name: 'Loans',
      dropdown: [
        'Home Loans',
        'Personal Loans',
        'Car Loans',
        'Two Wheeler Loans',
        'Loan Against Property'
      ]
    },
    {
      name: 'Cards',
      dropdown: [
        'Credit Cards',
        'Debit Cards',
        'Prepaid Cards',
        'Forex Cards'
      ]
    },
    {
      name: 'Investments',
      dropdown: [
        'Mutual Funds',
        'Life Insurance',
        'General Insurance',
        'Fixed Deposits',
        'Bonds'
      ]
    },
    {
      name: 'NRI Services',
      dropdown: [
        'NRI Accounts',
        'NRI Deposits',
        'NRI Loans',
        'Money Transfer'
      ]
    },
    {
      name: 'Corporate Banking',
      dropdown: [
        'Current Account',
        'Trade Services',
        'Cash Management',
        'Working Capital'
      ]
    }
  ];

  return (
    <header className="bg-white shadow-lg sticky top-0 z-50">
      {/* Top Bar */}
      <div className="bg-blue-900 text-white py-2">
        <div className="container mx-auto px-4 flex justify-between items-center text-sm">
          <div className="flex items-center space-x-4">
            <span className="flex items-center">
              <PhoneIcon className="h-4 w-4 mr-1" />
              1800-425-4332
            </span>
          </div>
          <div className="flex items-center space-x-4">
            <span>24x7 Customer Care</span>
            <span>|</span>
            <span>Find Branch/ATM</span>
          </div>
        </div>
      </div>

      {/* Main Header */}
      <div className="container mx-auto px-4">
        <div className="flex items-center justify-between py-4">
          {/* Logo */}
          <div className="flex items-center">
            <div className="flex items-center space-x-2">
              <div className="w-12 h-12 bg-blue-600 rounded border-2 border-white shadow-lg flex items-center justify-center">
                <div className="w-6 h-6 bg-red-500 rounded"></div>
              </div>
              <div>
                <h1 className="text-2xl font-bold text-blue-600">HDFC Bank</h1>
                <p className="text-xs text-gray-500">We understand your world</p>
              </div>
            </div>
          </div>

          {/* Desktop Navigation */}
          <nav className="hidden lg:flex items-center space-x-8">
            {navigationItems.map((item, index) => (
              <div
                key={index}
                className="relative"
                onMouseEnter={() => setActiveDropdown(index)}
                onMouseLeave={() => setActiveDropdown(null)}
              >
                <button className="flex items-center text-gray-700 hover:text-blue-600 transition-colors py-2">
                  {item.name}
                  <ChevronDownIcon className="h-4 w-4 ml-1" />
                </button>
                
                {activeDropdown === index && (
                  <motion.div
                    initial={{ opacity: 0, y: -10 }}
                    animate={{ opacity: 1, y: 0 }}
                    className="absolute top-full left-0 bg-white shadow-xl rounded-lg p-4 min-w-48 z-50"
                  >
                    {item.dropdown.map((subItem, subIndex) => (
                      <a
                        key={subIndex}
                        href="#"
                        className="block py-2 px-3 text-gray-600 hover:text-blue-600 hover:bg-blue-50 rounded transition-colors"
                      >
                        {subItem}
                      </a>
                    ))}
                  </motion.div>
                )}
              </div>
            ))}
          </nav>

          {/* Right Side Actions */}
          <div className="flex items-center space-x-4">
            <div className="hidden md:flex items-center">
              <div className="relative">
                <input
                  type="text"
                  placeholder="Search..."
                  className="pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <MagnifyingGlassIcon className="h-5 w-5 text-gray-400 absolute left-3 top-1/2 transform -translate-y-1/2" />
              </div>
            </div>
            
            <button className="bg-red-600 text-white px-6 py-2 rounded-lg hover:bg-red-700 transition-colors font-medium">
              NetBanking
            </button>
            
            <button className="border border-blue-600 text-blue-600 px-6 py-2 rounded-lg hover:bg-blue-600 hover:text-white transition-colors font-medium">
              Login
            </button>

            {/* Mobile Menu Button */}
            <button
              className="lg:hidden"
              onClick={() => setIsMenuOpen(!isMenuOpen)}
            >
              {isMenuOpen ? (
                <XMarkIcon className="h-6 w-6" />
              ) : (
                <Bars3Icon className="h-6 w-6" />
              )}
            </button>
          </div>
        </div>

        {/* Mobile Menu */}
        {isMenuOpen && (
          <motion.div
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: 'auto' }}
            className="lg:hidden border-t bg-white"
          >
            <div className="py-4 space-y-2">
              {navigationItems.map((item, index) => (
                <div key={index} className="border-b pb-2">
                  <button className="text-gray-700 font-medium py-2 px-4 w-full text-left">
                    {item.name}
                  </button>
                  <div className="pl-8 space-y-1">
                    {item.dropdown.slice(0, 3).map((subItem, subIndex) => (
                      <a
                        key={subIndex}
                        href="#"
                        className="block py-1 text-gray-600 text-sm"
                      >
                        {subItem}
                      </a>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </motion.div>
        )}
      </div>
    </header>
  );
};

// Hero Section
const HeroSection = () => {
  const [currentSlide, setCurrentSlide] = useState(0);

  const slides = [
    {
      title: "Digital Banking Made Simple",
      subtitle: "Experience seamless banking with HDFC Bank's digital solutions",
      buttonText: "Open Account",
      image: "https://images.unsplash.com/photo-1586108562388-f392017e9cda"
    },
    {
      title: "Home Loans at Attractive Rates",
      subtitle: "Fulfill your dream of owning a home with our competitive rates",
      buttonText: "Apply Now",
      image: "https://images.pexels.com/photos/7616608/pexels-photo-7616608.jpeg"
    },
    {
      title: "Credit Cards with Rewards",
      subtitle: "Earn rewards on every purchase with HDFC Bank Credit Cards",
      buttonText: "Explore Cards",
      image: "https://images.pexels.com/photos/5475750/pexels-photo-5475750.jpeg"
    }
  ];

  return (
    <section className="relative h-96 md:h-[500px] overflow-hidden">
      <div className="absolute inset-0">
        <img
          src={slides[currentSlide].image}
          alt="Banking Services"
          className="w-full h-full object-cover"
        />
        <div className="absolute inset-0 bg-blue-900 bg-opacity-60"></div>
      </div>
      
      <div className="relative h-full flex items-center">
        <div className="container mx-auto px-4">
          <div className="max-w-2xl text-white">
            <motion.h1
              key={currentSlide}
              initial={{ opacity: 0, y: 30 }}
              animate={{ opacity: 1, y: 0 }}
              className="text-4xl md:text-6xl font-bold mb-4"
            >
              {slides[currentSlide].title}
            </motion.h1>
            <motion.p
              key={`subtitle-${currentSlide}`}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.2 }}
              className="text-xl mb-8"
            >
              {slides[currentSlide].subtitle}
            </motion.p>
            <motion.button
              key={`button-${currentSlide}`}
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ delay: 0.4 }}
              className="bg-red-600 text-white px-8 py-3 rounded-lg text-lg font-medium hover:bg-red-700 transition-colors"
            >
              {slides[currentSlide].buttonText}
            </motion.button>
          </div>
        </div>
      </div>

      {/* Slide Indicators */}
      <div className="absolute bottom-6 left-1/2 transform -translate-x-1/2 flex space-x-2">
        {slides.map((_, index) => (
          <button
            key={index}
            className={`w-3 h-3 rounded-full transition-colors ${
              currentSlide === index ? 'bg-white' : 'bg-white bg-opacity-50'
            }`}
            onClick={() => setCurrentSlide(index)}
          />
        ))}
      </div>
    </section>
  );
};

// Quick Services Section
const QuickServices = () => {
  const services = [
    {
      icon: BanknotesIcon,
      title: "Open Account",
      description: "Start your banking journey",
      link: "#",
      color: "bg-blue-500"
    },
    {
      icon: CreditCardIcon,
      title: "Apply for Credit Card",
      description: "Get rewarded for every spend",
      link: "#",
      color: "bg-red-500"
    },
    {
      icon: BuildingOfficeIcon,
      title: "Home Loan",
      description: "Make your dream home a reality",
      link: "#",
      color: "bg-green-500"
    },
    {
      icon: ChartBarIcon,
      title: "Investments",
      description: "Grow your wealth smartly",
      link: "#",
      color: "bg-purple-500"
    },
    {
      icon: ComputerDesktopIcon,
      title: "NetBanking",
      description: "Bank online 24x7",
      link: "#",
      color: "bg-indigo-500"
    },
    {
      icon: PhoneIcon,
      title: "Mobile Banking",
      description: "Bank on the go",
      link: "#",
      color: "bg-orange-500"
    }
  ];

  return (
    <section className="py-16 bg-gray-50">
      <div className="container mx-auto px-4">
        <div className="text-center mb-12">
          <h2 className="text-3xl md:text-4xl font-bold text-gray-800 mb-4">
            Quick Services
          </h2>
          <p className="text-gray-600 text-lg">
            Access our most popular banking services instantly
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {services.map((service, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.1 }}
              className="bg-white rounded-xl shadow-lg p-6 hover:shadow-xl transition-shadow cursor-pointer group"
            >
              <div className={`w-12 h-12 ${service.color} rounded-lg flex items-center justify-center mb-4 group-hover:scale-110 transition-transform`}>
                <service.icon className="h-6 w-6 text-white" />
              </div>
              <h3 className="text-xl font-semibold text-gray-800 mb-2">
                {service.title}
              </h3>
              <p className="text-gray-600 mb-4">
                {service.description}
              </p>
              <button className="text-blue-600 font-medium hover:text-blue-800 transition-colors">
                Learn More →
              </button>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
};

// Products Section
const ProductsSection = () => {
  const products = [
    {
      title: "Personal Banking",
      description: "Comprehensive banking solutions for individuals",
      image: "https://images.pexels.com/photos/9169180/pexels-photo-9169180.jpeg",
      features: ["Savings Account", "Current Account", "Fixed Deposits", "Digital Banking"]
    },
    {
      title: "Loans & Mortgages",
      description: "Competitive rates for all your financing needs",
      image: "https://images.unsplash.com/photo-1574288061782-da2d3f79a72e",
      features: ["Home Loans", "Personal Loans", "Car Loans", "Education Loans"]
    },
    {
      title: "Investment Solutions",
      description: "Grow your wealth with our investment products",
      image: "https://images.unsplash.com/photo-1582244026359-2687d83af971",
      features: ["Mutual Funds", "Insurance", "Fixed Deposits", "SIP"]
    }
  ];

  return (
    <section className="py-16">
      <div className="container mx-auto px-4">
        <div className="text-center mb-12">
          <h2 className="text-3xl md:text-4xl font-bold text-gray-800 mb-4">
            Our Products & Services
          </h2>
          <p className="text-gray-600 text-lg">
            Discover banking solutions tailored for you
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {products.map((product, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.2 }}
              className="bg-white rounded-xl shadow-lg overflow-hidden hover:shadow-xl transition-shadow"
            >
              <div className="relative h-48">
                <img
                  src={product.image}
                  alt={product.title}
                  className="w-full h-full object-cover"
                />
                <div className="absolute inset-0 bg-blue-600 bg-opacity-20"></div>
              </div>
              <div className="p-6">
                <h3 className="text-xl font-semibold text-gray-800 mb-2">
                  {product.title}
                </h3>
                <p className="text-gray-600 mb-4">
                  {product.description}
                </p>
                <ul className="space-y-2 mb-6">
                  {product.features.map((feature, featureIndex) => (
                    <li key={featureIndex} className="flex items-center text-gray-600">
                      <span className="w-2 h-2 bg-blue-500 rounded-full mr-3"></span>
                      {feature}
                    </li>
                  ))}
                </ul>
                <button className="w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition-colors">
                  Explore Products
                </button>
              </div>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
};

// Features Section
const FeaturesSection = () => {
  const features = [
    {
      icon: ShieldCheckIcon,
      title: "Secure Banking",
      description: "Advanced security measures to protect your finances"
    },
    {
      icon: ComputerDesktopIcon,
      title: "Digital First",
      description: "Seamless digital banking experience across all platforms"
    },
    {
      icon: PhoneIcon,
      title: "24x7 Support",
      description: "Round-the-clock customer support for all your needs"
    },
    {
      icon: ChartBarIcon,
      title: "Investment Advisory",
      description: "Expert guidance for your investment decisions"
    }
  ];

  return (
    <section className="py-16 bg-blue-50">
      <div className="container mx-auto px-4">
        <div className="text-center mb-12">
          <h2 className="text-3xl md:text-4xl font-bold text-gray-800 mb-4">
            Why Choose HDFC Bank?
          </h2>
          <p className="text-gray-600 text-lg">
            Experience banking excellence with our premium features
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-8">
          {features.map((feature, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ delay: index * 0.1 }}
              className="text-center"
            >
              <div className="w-16 h-16 bg-blue-600 rounded-full flex items-center justify-center mx-auto mb-4">
                <feature.icon className="h-8 w-8 text-white" />
              </div>
              <h3 className="text-xl font-semibold text-gray-800 mb-2">
                {feature.title}
              </h3>
              <p className="text-gray-600">
                {feature.description}
              </p>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
};

// Customer Testimonials
const TestimonialsSection = () => {
  const testimonials = [
    {
      name: "Rajesh Kumar",
      role: "Business Owner",
      image: "https://images.unsplash.com/photo-1544717297-fa95b6ee9643",
      testimonial: "HDFC Bank has been my trusted banking partner for over 10 years. Their service quality is exceptional."
    },
    {
      name: "Priya Sharma",
      role: "Software Engineer",
      image: "https://images.pexels.com/photos/5475811/pexels-photo-5475811.jpeg",
      testimonial: "The digital banking experience is seamless. I can manage all my finances from anywhere, anytime."
    },
    {
      name: "Amit Patel",
      role: "Entrepreneur",
      image: "https://images.unsplash.com/photo-1544717297-fa95b6ee9643",
      testimonial: "Their loan approval process is quick and transparent. Got my business loan approved within 48 hours."
    }
  ];

  return (
    <section className="py-16">
      <div className="container mx-auto px-4">
        <div className="text-center mb-12">
          <h2 className="text-3xl md:text-4xl font-bold text-gray-800 mb-4">
            What Our Customers Say
          </h2>
          <p className="text-gray-600 text-lg">
            Trusted by millions of customers across India
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {testimonials.map((testimonial, index) => (
            <motion.div
              key={index}
              initial={{ opacity: 0, scale: 0.9 }}
              whileInView={{ opacity: 1, scale: 1 }}
              transition={{ delay: index * 0.1 }}
              className="bg-white rounded-xl shadow-lg p-6"
            >
              <div className="flex items-center mb-4">
                <img
                  src={testimonial.image}
                  alt={testimonial.name}
                  className="w-12 h-12 rounded-full object-cover mr-4"
                />
                <div>
                  <h4 className="font-semibold text-gray-800">{testimonial.name}</h4>
                  <p className="text-gray-600 text-sm">{testimonial.role}</p>
                </div>
              </div>
              <p className="text-gray-600 italic">"{testimonial.testimonial}"</p>
              <div className="flex text-yellow-400 mt-4">
                {'★'.repeat(5)}
              </div>
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
};

// Footer
const Footer = () => {
  const footerSections = [
    {
      title: "Personal Banking",
      links: ["Savings Account", "Current Account", "Fixed Deposits", "Recurring Deposits", "Salary Account"]
    },
    {
      title: "Loans",
      links: ["Home Loans", "Personal Loans", "Car Loans", "Two Wheeler Loans", "Loan Against Property"]
    },
    {
      title: "Cards",
      links: ["Credit Cards", "Debit Cards", "Prepaid Cards", "Forex Cards", "Commercial Cards"]
    },
    {
      title: "Investments",
      links: ["Mutual Funds", "Life Insurance", "General Insurance", "Fixed Deposits", "Bonds"]
    },
    {
      title: "Support",
      links: ["Customer Care", "Branch Locator", "ATM Locator", "Contact Us", "Grievances"]
    }
  ];

  return (
    <footer className="bg-gray-900 text-white py-12">
      <div className="container mx-auto px-4">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-8 mb-8">
          {footerSections.map((section, index) => (
            <div key={index}>
              <h3 className="font-semibold text-lg mb-4">{section.title}</h3>
              <ul className="space-y-2">
                {section.links.map((link, linkIndex) => (
                  <li key={linkIndex}>
                    <a href="#" className="text-gray-400 hover:text-white transition-colors">
                      {link}
                    </a>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </div>

        <div className="border-t border-gray-700 pt-8">
          <div className="flex flex-col md:flex-row justify-between items-center">
            <div className="flex items-center mb-4 md:mb-0">
              <div className="flex items-center space-x-2">
                <div className="w-8 h-8 bg-blue-600 rounded border border-white flex items-center justify-center">
                  <div className="w-3 h-3 bg-red-500 rounded"></div>
                </div>
                <span className="text-xl font-bold">HDFC Bank</span>
              </div>
            </div>
            
            <div className="flex items-center space-x-6">
              <span className="text-gray-400">Follow us:</span>
              <div className="flex space-x-4">
                <a href="#" className="text-gray-400 hover:text-white transition-colors">Facebook</a>
                <a href="#" className="text-gray-400 hover:text-white transition-colors">Twitter</a>
                <a href="#" className="text-gray-400 hover:text-white transition-colors">LinkedIn</a>
                <a href="#" className="text-gray-400 hover:text-white transition-colors">Instagram</a>
              </div>
            </div>
          </div>
          
          <div className="mt-8 pt-8 border-t border-gray-700 text-center text-gray-400">
            <p>&copy; 2024 HDFC Bank Ltd. All rights reserved. Terms & Conditions | Privacy Policy | Disclaimer</p>
          </div>
        </div>
      </div>
    </footer>
  );
};

// Main Homepage Component
export const HDFCBankHomePage = () => {
  return (
    <div className="min-h-screen">
      <Header />
      <HeroSection />
      <QuickServices />
      <ProductsSection />
      <FeaturesSection />
      <TestimonialsSection />
      <Footer />
    </div>
  );
};