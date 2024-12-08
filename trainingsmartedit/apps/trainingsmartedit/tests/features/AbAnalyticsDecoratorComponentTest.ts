/*
 * Copyright (c) 2022 SAP SE or an SAP affiliate company. All rights reserved.
 */
import { AbAnalyticsDecoratorComponent } from 'trainingsmartedit/abAnalyticsDecorator/AbAnalyticsDecoratorComponent';
import { AbAnalyticsService } from 'trainingsmarteditcommons';

describe('abAnalyticsDecoratorController', () => {
    let abAnalyticsService: jasmine.SpyObj<AbAnalyticsService>;

    let component: AbAnalyticsDecoratorComponent;
    beforeEach(() => {
        abAnalyticsService = jasmine.createSpyObj<AbAnalyticsService>('abAnalyticsService', [
            'getABAnalyticsForComponent'
        ]);

        component = new AbAnalyticsDecoratorComponent(abAnalyticsService);
    });

    it('should build a human readable AB analytics string', async () => {
        abAnalyticsService.getABAnalyticsForComponent.and.returnValue(
            Promise.resolve({
                aValue: 30,
                bValue: 70
            })
        );
        await component.ngOnInit();

        // Assert
        expect(component.abAnalytics).toBe('A: 30 B: 70');
    });
});
