clear all;
close all;


x1 = load('fixed_X.txt')';
y1 = load('fixed_Data.txt')';
e1 = std(y1)*ones(size(y1));

y2 = load('system1_Data.txt')';
x2 = load('system1_X.txt')';
e2 = std(y2)*ones(size(y2));


x1 =(x1/60/60); 
x2 = (x2/60/60);

% Create figure
figure1 = figure;

% Create axes
axes1 = axes('Parent',figure1,'LineWidth',3,'FontSize',16);
hold(axes1,'all');



%%

z1 = y2 + 1*e2;
z2 = y2 - 1*e2;
%p1 = plot(x2,y2,x2,z1,x2,z2); 

% �rea entre z1 e z2, sendo z1 >= z2.
delta_zb = z1-z2;
Zb = [z2',delta_zb'];

hb = area(x2,Zb);
set(hb(1),'FaceColor',[1 1 1]);
set(hb(1),'LineStyle','none');
set(hb(2),'FaceColor',[0.105882354080677 0.309803932905197 0.207843139767647]);
set(hb(2),'LineStyle','none');

hold on
p1 = plot(x2,y2,'linewidth',2, 'Color', [0 0.5 0]);

hold on

%%

z1 = y1 + 1*e1;
z2 = y1 - 1*e1;


%p = plot(x1,y1,x1,z1,x1,z2);
% set(p(1),'LineWidth',1);
% set(p(2),'LineWidth',2);
% set(p(3),'LineWidth',3);





% �rea entre z1 e z2, sendo z1 >= z2.
delta_za = z1-z2;
Za = [z2',delta_za'];
alpha(.3);
ha = area(x1,Za);set(ha(1),'LineStyle','none');
set(ha(2),'LineStyle','none');
set(ha(2),...
    'FaceColor',[1 0.800000011920929 0.800000011920929]);

hold on
p = plot(x1,y1,'linewidth',2, 'Color', 'r');

hold on
set(ha(1),'FaceColor',[1 1 1]);


%%



xlabel('Time (Hours)'), ylabel('Mean travel time')
legend([p ha(2) p1 hb(2)], 'Fixed', strcat(setstr(177),'1 Std Dev - Fixed'), 'System 1', strcat(setstr(177),'1 Std Dev - System 1'))

% Create xlabel
xlabel('Time (h)','FontSize',16);

% Create ylabel
ylabel('Mean Travel Time (s)','FontSize',16);

% Create legend
legend1 = legend(axes1,'show');
set(legend1,...
    'Position',[0.767881944444444 0.242276422764228 0.121354166666667 0.187533875338753],...
    'LineWidth',3);



