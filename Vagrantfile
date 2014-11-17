# vi:ft=ruby:

hostname = "gatling"

nodes = [
  {:hostname => 'server', :ip => '192.168.20.240', :ssh_port => 2267, :memory => 2048},
  {:hostname => 'client1', :ip => '192.168.20.241', :ssh_port => 2268},
  {:hostname => 'client2', :ip => '192.168.20.242', :ssh_port => 2269},
  {:hostname => 'client3', :ip => '192.168.20.243', :ssh_port => 2270},
  {:hostname => 'client4', :ip => '192.168.20.244', :ssh_port => 2271},
  {:hostname => 'client5', :ip => '192.168.20.245', :ssh_port => 2272}
]

def get_proxy
  remote_commands = <<-EOF
    test -f /etc/apt/apt.conf.d/000apt-cacher-ng-proxy || \
    echo "Acquire::http { Proxy \\"#{ENV['APT_PROXY']}\\"; };" \
    | sudo tee /etc/apt/apt.conf.d/000apt-cacher-ng-proxy
  EOF
end

def update_packages
 "sudo apt-get update -y -qq"
end

def get_config_tuning
  remote_commands = <<-EOF
    test -f /etc/security/limits.d/60-exploitation.conf || \
    echo "vagrant      soft    nofile          200000\n" \
         "vagrant      hard    nofile          200000" | \
    sudo tee /etc/security/limits.d/60-exploitation.conf
    test -f /etc/sysctl.d/60-network_portrange.conf || \
    echo "net.ipv4.ip_local_port_range=1024 65535\n"\
    "net.core.optmenm_max=25165824\n"\
    "#\n"\
    "# 32MB per socket - which sounds like a lot, but will virtually never\n"\
    "# consume that much.\n"\
    "#\n"\
    "net.core.rmem_max = 33554432\n"\
    "net.core.wmem_max = 33554432\n"\
    "net.core.rmem_default = 33554432\n"\
    "net.core.wmem_default = 33554432\n"\
    "# increase Linux autotuning TCP buffer limit to 32MB\n"\
    "net.ipv4.tcp_rmem = 4096 87380 33554432\n"\
    "net.ipv4.tcp_wmem = 4096 65536 33554432\n"\
    "\n"\
    "# Increase the number of outstanding syn requests allowed.\n"\
    "# c.f. The use of syncookies.\n"\
    "net.ipv4.tcp_max_syn_backlog = 4096\n"\
    "net.ipv4.tcp_syncookies = 1\n"\
    "net.core.netdev_max_backlog= 4096\n"\
    "\n"\
    "# The maximum number of "backlogged sockets".  Default is 128.\n"\
    "net.core.somaxconn = 8192\n"\
    "fs.file-max = 400000\n" | \
    sudo tee /etc/sysctl.d/60-network_portrange.conf
  EOF
end

def multiple_ip
  remote_commands = <<-EOF
  for i in `seq 1 10`; do sudo ifconfig eth1:$i 192.168.48.$i up ; done
  EOF
end

def client_routing
  remote_commands = <<-EOF
  sudo ip r a 192.168.48.0/24 via 192.168.20.240
  EOF
end

def install_gatling
  remote_commands = <<-EOF
    echo "gem: --no-ri --no-rdoc" >> ~/.gemrc
    if ! [ -f gatling-charts-highcharts-2.0.2-bundle.zip ] ; then
      wget -nv http://repo1.maven.org/maven2/io/gatling/highcharts/gatling-charts-highcharts/2.0.2/gatling-charts-highcharts-2.0.2-bundle.zip \
         -O gatling-charts-highcharts-2.0.2-bundle.zip
    fi
    if ! [ -d gatling-charts-highcharts-2.0.2 ] ; then
      unzip gatling-charts-highcharts-2.0.2-bundle.zip
    fi
    if ! [ -L gatling ] ; then
      ln -s gatling-charts-highcharts-2.0.2 gatling
    fi
    sudo chown -R vagrant:vagrant gatling-charts-highcharts-2.0.2
    dpkg -s openjdk-7-jdk 2>/dev/null || \
      sudo DEBIAN_FRONTEND=noninteractive apt-get install -y rsync openjdk-7-jdk
  EOF
end

def install_erlang
  remote_commands = <<-EOF
    test -f /etc/apt/sources.list.d/erlang.list || \
    echo "deb http://packages.erlang-solutions.com/debian wheezy contrib" | \
    sudo tee /etc/apt/sources.list.d/erlang.list
    wget -qO - \
      http://packages.erlang-solutions.com/debian/erlang_solutions.asc | \
      sudo apt-key add -
    sudo apt-get update -y -qq
    dpkg -s erlang 2>/dev/null || \
      sudo DEBIAN_FRONTEND=noninteractive apt-get install -y rsync erlang erlang-base-hipe
  EOF
end

Vagrant.configure("2") do |config|

  config.vm.box = "dwwheezy64"
  config.vm.box_url = 'http://review.deveryware.net/boxes/dwwheezy64.box'

  nodes.each do |node|
    config.vm.define node[:hostname] do |node_config|
      node_config.vm.hostname = node[:hostname]
      node_config.vm.network :public_network, :bridge => 'en0: Ethernet 1', ip: node[:ip]

      memory = node[:memory] || 1024
      # More memory than the default, since we're running a lot of stuff (512M)
      config.vm.provider :virtualbox do |v|
        v.customize ["modifyvm", :id, "--memory", memory]
        #     v.customize ["modifyvm", :id, "--natdnshostresolver1", "on"]
        #     v.customize ["modifyvm", :id, "--natdnsproxy1", "on"]
      end
      # Have ssh be accessible through port 2267. Hard coding this so we don't collide with other vagrant vms.
      node_config.vm.network :forwarded_port, guest: 22, host: node[:ssh_port]
      node_config.ssh.port = node[:ssh_port]

      commands = []
      if ENV['APT_PROXY']
        commands << get_proxy
      end
      commands << update_packages
      commands << get_config_tuning
      if node[:hostname] == 'server'
        commands << install_erlang
        commands << multiple_ip
      else
        commands << install_gatling
        commands << client_routing
      end
      commands.each do |c|
        node_config.vm.provision :shell, :inline => c
      end
      if node[:hostname] != 'server'
        node_config.vm.provision :file, source: 'gatling.conf', destination: 'gatling/conf/conf/gatling.conf'
      end
    end
  end

end
